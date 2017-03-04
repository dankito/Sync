package net.dankito.sync.synchronization;

import net.dankito.sync.SyncEntity;
import net.dankito.sync.SyncEntityLocalLookupKeys;
import net.dankito.sync.SyncModuleConfiguration;
import net.dankito.sync.persistence.IEntityManager;


public class EntityPersistenceHandler {

  protected IEntityManager entityManager;


  public EntityPersistenceHandler(IEntityManager entityManager) {
    this.entityManager = entityManager;
  }


  public SyncEntityLocalLookupKeys persistEntryLookupKey(SyncModuleConfiguration syncModuleConfiguration, SyncEntity entity) {
    SyncEntityLocalLookupKeys lookupKeyEntry = new SyncEntityLocalLookupKeys(getSyncEntityType(entity), entity.getId(),
        entity.getLocalLookupKey(), entity.getLastModifiedOnDevice(), syncModuleConfiguration);
    entityManager.persistEntity(lookupKeyEntry);

    return lookupKeyEntry;
  }

  protected String getSyncEntityType(SyncEntity entity) {
    return entity.getClass().getName();
  }


  public void deleteEntityLookupKey(SyncEntityLocalLookupKeys lookupKey) {
    entityManager.deleteEntity(lookupKey);
  }

}
