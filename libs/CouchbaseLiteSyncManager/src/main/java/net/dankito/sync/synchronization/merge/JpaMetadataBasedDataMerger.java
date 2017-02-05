package net.dankito.sync.synchronization.merge;


import net.dankito.jpa.annotationreader.config.PropertyConfig;
import net.dankito.jpa.annotationreader.config.inheritance.DiscriminatorColumnConfig;
import net.dankito.jpa.couchbaselite.Dao;
import net.dankito.sync.BaseEntity;
import net.dankito.sync.SyncEntity;
import net.dankito.sync.persistence.CouchbaseLiteEntityManagerBase;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;

import javax.inject.Named;


@Named
public class JpaMetadataBasedDataMerger implements IDataMerger {

  private static final Logger log = LoggerFactory.getLogger(JpaMetadataBasedDataMerger.class);


  protected CouchbaseLiteEntityManagerBase entityManager;


  public JpaMetadataBasedDataMerger(CouchbaseLiteEntityManagerBase entityManager) {
    this.entityManager = entityManager;
  }


  public boolean mergeEntityData(Object updateSink, Object updateSource) {
    if(updateSink != null && updateSource != null) {
      Dao dao = entityManager.getDaoForClass(updateSink.getClass());
      if(dao != null) {
        for(PropertyConfig property : dao.getEntityConfig().getPropertiesIncludingInheritedOnes()) {
          if(isSystemProperty(property) == false && isPropertyOfBaseEntity(property) == false) {
            handleProperty(updateSink, updateSource, dao, property);
          }
        }

        return true;
      }
    }

    return false;
  }

  protected boolean isSystemProperty(PropertyConfig property) {
    return property.isId() || property.isVersion() || property instanceof DiscriminatorColumnConfig;
  }

  protected boolean isPropertyOfBaseEntity(PropertyConfig property) {
    return BaseEntity.class.equals(property.getField().getDeclaringClass());
  }

  protected void handleProperty(Object updateSink, Object updateSource, Dao dao, PropertyConfig property) {
    try {
      Object sourcePropertyValue = dao.extractValueFromObject(updateSource, property);

      if(property.isCollectionProperty()) {
        mergeCollectionProperty(updateSink, (Collection)sourcePropertyValue, dao, property);
      }
      else if(property.isRelationshipProperty()) {
        mergeRelationshipProperty(updateSink, sourcePropertyValue, dao, property);
      }
      else {
        mergeProperty(updateSink, sourcePropertyValue, dao, property);
      }
    } catch(Exception e) {
      log.error("Could not update Property " + property + " on object " + updateSink + " from source " + updateSource);
    }
  }

  protected void mergeProperty(Object updateSink, Object sourcePropertyValue, Dao dao, PropertyConfig property) {
    try {
      dao.setValueOnObject(updateSink, property, sourcePropertyValue);
    } catch(Exception e) {
      log.error("Could not update Property " + property + " on object " + updateSink + " from source " + sourcePropertyValue);
    }
  }

  protected void mergeRelationshipProperty(Object updateSink, Object sourcePropertyValue, Dao dao, PropertyConfig property) throws SQLException {
    Object sinkPropertyValue = dao.extractValueFromObject(updateSink, property);

    mergeEntityData(sinkPropertyValue, sourcePropertyValue);
  }

  protected void mergeCollectionProperty(Object updateSink, Collection<BaseEntity> sourcePropertyCollection, Dao dao, PropertyConfig property) throws SQLException {
    Collection<BaseEntity> sinkPropertyCollection = (Collection)dao.extractValueFromObject(updateSink, property);

    for(BaseEntity sinkCollectionItem : new ArrayList<>(sinkPropertyCollection)) {
      BaseEntity matchingEntity = findMatchingEntity(sinkCollectionItem, sourcePropertyCollection);

      if(matchingEntity == null) {
        sinkPropertyCollection.remove(sinkCollectionItem);
      }
      else {
        mergeEntityData(sinkCollectionItem, matchingEntity);
      }
    }

    for(BaseEntity sourceCollectionItem : new ArrayList<>(sourcePropertyCollection)) {
      BaseEntity matchingEntity = findMatchingEntity(sourceCollectionItem, sinkPropertyCollection);

      if(matchingEntity == null) {
        entityManager.persistEntity(sourceCollectionItem);
        sinkPropertyCollection.add(sourceCollectionItem);
      }
    }
  }

  protected BaseEntity findMatchingEntity(BaseEntity itemToMatch, Collection<BaseEntity> collection) {
    for(BaseEntity item : collection) {
      if(item.getId() != null && item.getId().equals(itemToMatch.getId())) {
        return item;
      }
      else if(itemToMatch instanceof SyncEntity && item instanceof SyncEntity) {
        SyncEntity syncEntityToMatch = (SyncEntity)itemToMatch;
        if(syncEntityToMatch.getLocalLookupKey() != null && syncEntityToMatch.getLocalLookupKey().equals(((SyncEntity)item).getLocalLookupKey())) {
          return item;
        }
      }
    }

    return null;
  }

}
