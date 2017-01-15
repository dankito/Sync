package net.dankito.sync.synchronization;

import com.couchbase.lite.Database;
import com.couchbase.lite.Document;
import com.couchbase.lite.DocumentChange;
import com.couchbase.lite.SavedRevision;

import net.dankito.jpa.annotationreader.config.EntityConfig;
import net.dankito.jpa.annotationreader.config.PropertyConfig;
import net.dankito.jpa.annotationreader.config.inheritance.DiscriminatorColumnConfig;
import net.dankito.jpa.couchbaselite.Dao;
import net.dankito.jpa.relationship.collections.EntitiesCollection;
import net.dankito.sync.BaseEntity;
import net.dankito.sync.config.DatabaseTableConfig;
import net.dankito.sync.persistence.CouchbaseLiteEntityManagerBase;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by ganymed on 14/09/16.
 */
public class SynchronizedDataMerger {

  private static final Logger log = LoggerFactory.getLogger(SynchronizedDataMerger.class);


  protected CouchbaseLiteSyncManager syncManager;

  protected CouchbaseLiteEntityManagerBase entityManager;

  protected Database database;


  public SynchronizedDataMerger(CouchbaseLiteSyncManager syncManager, CouchbaseLiteEntityManagerBase entityManager, Database database) {
    this.syncManager = syncManager;
    this.entityManager = entityManager;
    this.database = database;
  }


  public BaseEntity updateCachedSynchronizedEntity(DocumentChange change, Class entityType) {
    BaseEntity cachedEntity = null;

    if(entityType != null) { // sometimes only some Couchbase internal data is synchronized without any user data -> skip these
      try {
        cachedEntity = (BaseEntity) entityManager.getObjectCache().get(entityType, change.getDocumentId());
        if(cachedEntity != null) { // cachedEntity == null: Entity not retrieved / cached yet -> will be read from DB on next access anyway, therefore no need to update it
          log.info("Updating cached synchronized Entity of Revision " + change.getRevisionId() + ": " + cachedEntity);

          Document storedDocument = database.getExistingDocument(change.getDocumentId());
          Dao dao = entityManager.getDaoForClass(entityType);

          List<SavedRevision> revisionHistory = storedDocument.getRevisionHistory();
          SavedRevision currentRevision = storedDocument.getCurrentRevision();

          updateCachedEntity(cachedEntity, dao, currentRevision);
        }
      } catch (Exception e) {
        log.error("Could not handle Change", e);
      }
    }

    if(isEntityDeleted(cachedEntity, change)) {
      if(cachedEntity == null) {
        cachedEntity = entityManager.getEntityById(entityType, change.getDocumentId());
      }
    }

    return cachedEntity;
  }


  protected boolean isEntityDeleted(BaseEntity cachedEntity, DocumentChange change) {
    if(cachedEntity != null) {
      return cachedEntity.isDeleted();
    }
    else {
      try {
        return (Boolean) change.getAddedRevision().getPropertyForKey(DatabaseTableConfig.BASE_ENTITY_DELETED_COLUMN_NAME);
      } catch(Exception ignored) { }
    }

    return false;
  }


  protected void updateCachedEntity(BaseEntity cachedEntity, Dao dao, SavedRevision currentRevision) throws SQLException {
    EntityConfig entityConfig = dao.getEntityConfig();
    Map<String, Object> detectedChanges = getChanges(cachedEntity, dao, entityConfig, currentRevision);

    if(detectedChanges.size() > 0) {
      for(String propertyName : detectedChanges.keySet()) {
        try {
          updateProperty(cachedEntity, propertyName, dao, entityConfig, currentRevision, detectedChanges);
        } catch(Exception e) {
          log.error("Could not update Property " + propertyName + " on synchronized Object " + cachedEntity, e);
        }
      }
    }
  }

  protected void updateProperty(BaseEntity cachedEntity, String propertyName, Dao dao, EntityConfig entityConfig, SavedRevision currentRevision, Map<String, Object> detectedChanges) throws SQLException {
    PropertyConfig property = entityConfig.getPropertyByColumnName(propertyName);
    Object previousValue = dao.extractValueFromObject(cachedEntity, property);

    if(property.isCollectionProperty() == false) {
      Object updatedValue = dao.deserializePersistedValue(cachedEntity, property, currentRevision.getProperty(propertyName));
      dao.setValueOnObject(cachedEntity, property, updatedValue);
    }
    else {
      updateCollectionProperty(cachedEntity, property, propertyName, currentRevision, detectedChanges, previousValue);
    }
  }

  protected void updateCollectionProperty(BaseEntity cachedEntity, PropertyConfig property, String propertyName, SavedRevision currentRevision, Map<String, Object> detectedChanges,
                                          Object previousValue) throws SQLException {
    String previousTargetEntityIdsString = (String)detectedChanges.get(propertyName);
    String currentTargetEntityIdsString = (String)currentRevision.getProperty(propertyName);
    if(currentRevision.getProperties().containsKey(propertyName) == false) { // currentRevision has no information about this property
      currentTargetEntityIdsString = "[]"; // TODO: what to do here? Assuming "[]" is for sure false. Removing all items?
    }

    Dao targetDao = entityManager.getDaoForClass(property.getTargetEntityClass());
    Collection<Object> currentTargetEntityIds = targetDao.parseJoinedEntityIdsFromJsonString(currentTargetEntityIdsString);

    log.info("Collection Property " + property + " of Revision " + currentRevision.getId() + " has now Ids of " + currentTargetEntityIdsString + ". Previous ones: " + previousTargetEntityIdsString);

    if(previousValue instanceof EntitiesCollection) { // TODO: what to do if it's not an EntitiesCollection yet?
      ((EntitiesCollection)previousValue).refresh(currentTargetEntityIds);
    }
    else {
      log.warn("Not an EntitiesCollection: " + previousValue);
    }
  }

  protected Map<String, Object> getChanges(BaseEntity cachedEntity, Dao dao, EntityConfig entityConfig, SavedRevision currentRevision) {
    Map<String, Object> detectedChanges = new HashMap<>();

    for(PropertyConfig propertyConfig : entityConfig.getPropertiesIncludingInheritedOnes()) {
      if(propertyConfig.isId() || propertyConfig.isVersion() || propertyConfig instanceof DiscriminatorColumnConfig ||
          DatabaseTableConfig.BASE_ENTITY_MODIFIED_ON_COLUMN_NAME.equals(propertyConfig.getColumnName())) {
        continue;
      }

      try {
        Object cachedEntityValue = dao.getPersistablePropertyValue(cachedEntity, propertyConfig);

        if(hasPropertyValueChanged(cachedEntity, propertyConfig, cachedEntityValue, dao, currentRevision)) {
          detectedChanges.put(propertyConfig.getColumnName(), cachedEntityValue);
        }
      } catch(Exception e) {
        log.error("Could not check Property " + propertyConfig + " for changes", e);
      }
    }

    return detectedChanges;
  }

  protected boolean hasPropertyValueChanged(BaseEntity cachedEntity, PropertyConfig propertyConfig, Object cachedEntityValue, Dao dao, SavedRevision currentRevision) throws SQLException {
    Object currentRevisionValue = currentRevision.getProperties().get(propertyConfig.getColumnName());

    if(propertyConfig.isLob()) {
      currentRevisionValue = dao.getLobFromAttachment(propertyConfig, currentRevision.getDocument());
      if(currentRevisionValue != cachedEntityValue) {
        dao.setValueOnObject(cachedEntity, propertyConfig, currentRevisionValue); // TODO: this produces a side effect, but i would have to change structure too hard to implement this little feature

        if(cachedEntityValue != null && cachedEntityValue instanceof byte[] && dao.shouldCompactDatabase(((byte[])cachedEntityValue).length)) {
          dao.compactDatabase();
        }
      }
    }
    else if(propertyConfig.isCollectionProperty() == false) {
      if ((cachedEntityValue == null && currentRevisionValue != null) || (cachedEntityValue != null && currentRevisionValue == null) ||
          (cachedEntityValue != null && cachedEntityValue.equals(currentRevisionValue) == false)) {
        return true;
      }
    }
    else {
      if(hasCollectionPropertyChanged(dao, currentRevisionValue, cachedEntityValue)) {
        return true;
      }
    }

    return false;
  }

  protected boolean hasCollectionPropertyChanged(Dao dao, Object currentRevisionValue, Object cachedEntityValue) throws SQLException {
    if(currentRevisionValue == null || cachedEntityValue == null) {
      return currentRevisionValue != cachedEntityValue; // if only one of them is null, than there's a change
    }

    Collection<Object> currentRevisionTargetEntityIds = dao.parseJoinedEntityIdsFromJsonString((String)currentRevisionValue);
    Collection<Object> cachedEntityTargetEntityIds = dao.parseJoinedEntityIdsFromJsonString((String)cachedEntityValue);

    if(currentRevisionTargetEntityIds.size() != cachedEntityTargetEntityIds.size()) {
      return true;
    }

    for(Object targetEntityId : currentRevisionTargetEntityIds) {
      if(cachedEntityTargetEntityIds.contains(targetEntityId) == false) {
        return true;
      }
    }

    return false; // cachedEntityTargetEntityIds contains all targetEntityIds of currentRevisionTargetEntityIds
  }

}
