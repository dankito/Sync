package net.dankito.sync.synchronization.merge;


import net.dankito.jpa.annotationreader.config.PropertyConfig;
import net.dankito.jpa.annotationreader.config.inheritance.DiscriminatorColumnConfig;
import net.dankito.jpa.couchbaselite.Dao;
import net.dankito.sync.BaseEntity;
import net.dankito.sync.persistence.CouchbaseLiteEntityManagerBase;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
            updateProperty(updateSink, updateSource, dao, property);
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

  protected void updateProperty(Object updateSink, Object updateSource, Dao dao, PropertyConfig property) {
    try {
      Object updatedValue = dao.extractValueFromObject(updateSource, property);
      dao.setValueOnObject(updateSink, property, updatedValue);
    } catch(Exception e) {
      log.error("Could not update Property " + property + " on object " + updateSink + " from source " + updateSource);
    }
  }

}
