package net.dankito.sync.synchronization;


import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.Database;
import com.couchbase.lite.Document;
import com.couchbase.lite.DocumentChange;
import com.couchbase.lite.SavedRevision;
import com.couchbase.lite.TransactionalTask;
import com.couchbase.lite.UnsavedRevision;

import net.dankito.jpa.annotationreader.config.EntityConfig;
import net.dankito.jpa.annotationreader.config.PropertyConfig;
import net.dankito.jpa.couchbaselite.Dao;
import net.dankito.sync.config.DatabaseTableConfig;
import net.dankito.sync.persistence.CouchbaseLiteEntityManagerBase;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class ConflictHandler {

  private static final Logger log = LoggerFactory.getLogger(ConflictHandler.class);


  protected CouchbaseLiteEntityManagerBase entityManager;

  protected Database database;


  public ConflictHandler(CouchbaseLiteEntityManagerBase entityManager, Database database) {
    this.entityManager = entityManager;
    this.database = database;
  }


  protected void handleConflict(DocumentChange change, Class entityType) {
    if(entityType != null) {
      try {
        Document storedDocument = database.getExistingDocument(change.getDocumentId());
        final List<SavedRevision> conflicts = storedDocument.getConflictingRevisions();

        if(conflicts.size() > 1) {
          log.info("Handling Conflict for " + entityType + " of Revision " + change.getRevisionId());

          Dao dao = entityManager.getDaoForClass(entityType);
          final String currentRevisionId = storedDocument.getCurrentRevisionId();

          final Map<String, Object> updatedProperties = new HashMap<>();
          updatedProperties.putAll(storedDocument.getCurrentRevision().getProperties());

          Map<String, Object> mergedProperties = mergeProperties(dao, conflicts);
          updatedProperties.putAll(mergedProperties);

          updateWinningRevisionDeleteConflictedOnes(conflicts, updatedProperties, currentRevisionId);
        }
      } catch (Exception e) {
        log.error("Could not handle conflict for Document Id " + change.getDocumentId() + " of Entity " + entityType, e);
      }
    }
  }

  protected void updateWinningRevisionDeleteConflictedOnes(final List<SavedRevision> conflicts, final Map<String, Object> updatedProperties, final String currentRevisionId) {
    // a modified version of http://labs.couchbase.com/couchbase-mobile-portal/develop/guides/couchbase-lite/native-api/document/index.html#Understanding%20Conflicts
    database.runInTransaction(new TransactionalTask() {
      @Override
      public boolean run() {
        boolean success = true;

        // Delete the conflicting revisions to get rid of the conflict:
        for (SavedRevision rev : conflicts) {
          success &= ConflictHandler.this.updateWinningRevisionDeleteConflictedOnes(rev, updatedProperties, currentRevisionId);
        }

        return success;
      }
    });
  }

  protected boolean updateWinningRevisionDeleteConflictedOnes(SavedRevision revision, Map<String, Object> updatedProperties, String currentRevisionId) {
    try {
      UnsavedRevision newRevision = revision.createRevision();

      if (revision.getId().equals(currentRevisionId)) {
        newRevision.setProperties(updatedProperties);
      }
      else {
        newRevision.setIsDeletion(true);
      }

      // saveAllowingConflict allows 'revision' to be updated even if it
      // is not the document's current revision.
      newRevision.save(true);

      return true;
    }
    catch (CouchbaseLiteException e) {
      log.error("Could not resolve conflict", e);
      return false;
    }
  }

  protected Map<String, Object> mergeProperties(Dao dao, List<SavedRevision> conflicts) {
    Map<String, Object> mergedProperties = new HashMap<>();
    EntityConfig entityConfig = dao.getEntityConfig();

    SavedRevision newerRevision = conflicts.get(0);
    SavedRevision olderRevision = conflicts.get(1);
    // get which one is newer
    if((Long)newerRevision.getProperty(DatabaseTableConfig.BASE_ENTITY_MODIFIED_ON_COLUMN_NAME) < (Long)olderRevision.getProperty(DatabaseTableConfig.BASE_ENTITY_MODIFIED_ON_COLUMN_NAME)) {
      newerRevision = olderRevision;
      olderRevision = conflicts.get(0);
    }

    SavedRevision commonParent = findCommonParent(newerRevision, olderRevision);
    Map<String, Object> commonParentProperties = new HashMap<>();
    if(commonParent != null) {
      commonParentProperties.putAll(commonParent.getProperties());
    }

    for(PropertyConfig property : entityConfig.getPropertiesIncludingInheritedOnes()) {
      try {
        mergeProperty(dao, property, newerRevision, olderRevision, commonParent, mergedProperties);
      } catch(Exception e) {
        log.error("Could not merge Property " + property + " on conflicted Entity of Type " + entityConfig.getEntityClass(), e);
      }
    }

    return mergedProperties;
  }

  protected SavedRevision findCommonParent(SavedRevision revision01, SavedRevision revision02) {
    try {
      List<SavedRevision> history01 = revision01.getRevisionHistory();
      List<SavedRevision> history02 = revision02.getRevisionHistory();

      for(int i = history01.size() - 1; i >= 0; i--) {
        SavedRevision parentRevision = history01.get(i);

        if(history02.contains(parentRevision)) {
          return parentRevision;
        }
      }
    } catch(Exception e) {
      log.error("Could not get common parent", e);
    }
    return null;
  }

  protected void mergeProperty(Dao dao, PropertyConfig property, SavedRevision newerRevision, SavedRevision olderRevision, SavedRevision commonParent, Map<String, Object> mergedProperties) throws SQLException {
    String propertyName = property.getColumnName();

    if(entityManager.isCouchbaseLiteSystemProperty(propertyName) == false) {
      Object newerValue = newerRevision.getProperty(propertyName);
      Object olderValue = olderRevision.getProperty(propertyName);

      if(property.isCollectionProperty() == false) {
        if ((newerValue != null && newerValue.equals(olderValue) == false) ||
            (newerValue == null && olderValue != null && newerRevision.getProperties().containsKey(propertyName))) { // only put a null value to mergedProperties if by this a previous value got deleted
          mergedProperties.put(propertyName, newerValue);
        } else if (olderValue != null && newerValue == null) {
          mergedProperties.put(propertyName, olderValue);
        }
      }
      else {
        mergeCollectionProperty(property, newerValue, olderValue, commonParent, mergedProperties);
      }
    }
  }

  protected void mergeCollectionProperty(PropertyConfig property, Object newerValue, Object olderValue, SavedRevision commonParent, Map<String, Object> mergedProperties) throws SQLException {
    Dao targetEntityDao = entityManager.getDaoForClass(property.getTargetEntityClass());

    Collection<Object> newerValueTargetEntityIds = targetEntityDao.parseJoinedEntityIdsFromJsonString((String)newerValue);
    Collection<Object> olderValueTargetEntityIds = targetEntityDao.parseJoinedEntityIdsFromJsonString((String)olderValue);

    Collection<Object> commonParentTargetEntityIds = new ArrayList<>(0);
    if(commonParent != null) {
      commonParentTargetEntityIds = targetEntityDao.parseJoinedEntityIdsFromJsonString((String) commonParent.getProperty(property.getColumnName()));
    }

    Set<Object> mergedEntityIds = new HashSet<>();
    mergedEntityIds.addAll(newerValueTargetEntityIds);
    mergedEntityIds.addAll(olderValueTargetEntityIds);

    for(Object targetEntityId : commonParentTargetEntityIds) {
      if(newerValueTargetEntityIds.contains(targetEntityId) == false || olderValueTargetEntityIds.contains(targetEntityId) == false) { // Entity has been deleted
//        mergedEntityIds.remove(targetEntityId);
      }
    }

    String mergedEntityIdsString = targetEntityDao.getPersistableCollectionTargetEntities(mergedEntityIds);
    mergedProperties.put(property.getColumnName(), mergedEntityIdsString);
  }

}
