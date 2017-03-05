package net.dankito.sync.synchronization;

import net.dankito.sync.ContactSyncEntity;
import net.dankito.sync.EmailSyncEntity;
import net.dankito.sync.FileSyncEntity;
import net.dankito.sync.LocalConfig;
import net.dankito.sync.PhoneNumberSyncEntity;
import net.dankito.sync.SyncEntity;
import net.dankito.sync.SyncEntityLocalLookupKeys;
import net.dankito.sync.SyncEntityState;
import net.dankito.sync.SyncJobItem;
import net.dankito.sync.SyncModuleConfiguration;
import net.dankito.sync.SyncState;
import net.dankito.sync.devices.DiscoveredDevice;
import net.dankito.sync.persistence.IEntityManager;
import net.dankito.sync.synchronization.files.FileSender;
import net.dankito.sync.synchronization.files.FileSyncJobItem;
import net.dankito.sync.synchronization.files.FileSyncServiceDefaultConfig;
import net.dankito.sync.synchronization.modules.HandleRetrievedSynchronizedEntityCallback;
import net.dankito.sync.synchronization.modules.HandleRetrievedSynchronizedEntityResult;
import net.dankito.sync.synchronization.modules.ISyncModule;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;


public class SynchronizedSyncJobItemsHandler {

  private static final Logger log = LoggerFactory.getLogger(SynchronizedSyncJobItemsHandler.class);


  protected IEntityManager entityManager;

  protected FileSender fileSender;

  protected LocalConfig localConfig;

  protected EntityPersistenceHandler persistenceHandler;

  protected SyncConfigurationManagerBase syncConfigurationManager;

  protected List<SyncEntity> syncEntitiesCurrentlyBeingSynchronized = new CopyOnWriteArrayList<>();


  public SynchronizedSyncJobItemsHandler(IEntityManager entityManager, FileSender fileSender, LocalConfig localConfig, EntityPersistenceHandler persistenceHandler, SyncConfigurationManagerBase syncConfigurationManager) {
    this.entityManager = entityManager;
    this.fileSender = fileSender;
    this.localConfig = localConfig;
    this.persistenceHandler = persistenceHandler;
    this.syncConfigurationManager = syncConfigurationManager;
  }


  public void handleSynchronizedSyncJobItem(SyncJobItem syncJobItem) {
    if(isInitializedSyncJobWeAreDestinationFor(syncJobItem)) {
      newSyncJobItemSynchronized(syncJobItem);
    }
    else if(areWeSourceOfSyncJobItem(syncJobItem)) {
      if(isFileRemoteAwaitsThatTransferStarts(syncJobItem)) {
        remoteRetrievedOurFileSyncJobItem(syncJobItem);
      }
    }
  }


  protected boolean isInitializedSyncJobWeAreDestinationFor(SyncJobItem syncJobItem) {
    return syncJobItem.getDestinationDevice() == localConfig.getLocalDevice() && syncJobItem.getState() == SyncState.INITIALIZED;
  }

  protected boolean areWeSourceOfSyncJobItem(SyncJobItem syncJobItem) {
    return syncJobItem.getSourceDevice() == localConfig.getLocalDevice();
  }

  protected boolean isFileRemoteAwaitsThatTransferStarts(SyncJobItem syncJobItem) {
    return syncJobItem.getEntity() instanceof FileSyncEntity && syncJobItem.getState() == SyncState.TRANSFERRING_FILE_TO_DESTINATION_DEVICE;
  }



  protected void newSyncJobItemSynchronized(final SyncJobItem jobItem) {
    SyncEntity entity = jobItem.getEntity();
    if(entity instanceof FileSyncEntity == false) {
      jobItem.setState(SyncState.TRANSFERRED_TO_DESTINATION_DEVICE);
      entityManager.updateEntity(jobItem);
    }

    final SyncModuleConfiguration syncModuleConfiguration = jobItem.getSyncModuleConfiguration();
    ISyncModule syncModule = syncConfigurationManager.getSyncModuleForSyncModuleConfiguration(syncModuleConfiguration);

    final SyncEntityState syncEntityState;
    final Map<String, SyncEntityLocalLookupKeys> allLookupKeys = getLookupKeysForSyncModuleConfigurationByDatabaseId(syncModuleConfiguration);
    SyncEntityLocalLookupKeys lookupKey = allLookupKeys.remove(entity.getId());
    if(lookupKey == null) {
      lookupKey = persistEntryLookupKey(syncModuleConfiguration, entity); // persist already here so that we know that we know SyncEntity with this database id
      syncEntityState = SyncEntityState.CREATED;
    }
    else {
      setSyncEntityLocalValuesFromLookupKey(entity, lookupKey);
      syncEntityState = getSyncEntityState(jobItem.getEntity(), lookupKey);
    }

    syncEntitiesCurrentlyBeingSynchronized.add(entity); // when calling handleRetrievedSynchronizedEntityAsync() shortly after syncEntityChangeListener gets called, but its local lookup key hasn't been stored yet to database at this point

    log.info("Retrieved synchronized entity " + jobItem.getEntity() + " of SyncEntityState " + syncEntityState);

    if(syncModule != null) {
      final SyncEntityLocalLookupKeys finalLookupKey = lookupKey;

      syncModule.handleRetrievedSynchronizedEntityAsync(jobItem, syncEntityState, new HandleRetrievedSynchronizedEntityCallback() {
        @Override
        public void done(HandleRetrievedSynchronizedEntityResult result) {
          if(result.isSuccessful()) { // TODO: what to do in error case?
            entitySuccessfullySynchronized(jobItem, finalLookupKey, syncEntityState, syncModuleConfiguration, allLookupKeys);
          }
        }
      });
    }

    syncEntitiesCurrentlyBeingSynchronized.remove(jobItem.getEntity()); // TODO: shouldn't this be called in done() (+ if syncModule == null)?
  }

  protected SyncEntityState getSyncEntityState(SyncEntity entity, SyncEntityLocalLookupKeys lookupKey) {
    if(lookupKey == null || lookupKey.getEntityLocalLookupKey() == null) {
      return SyncEntityState.CREATED;
    }
    else {
      if(entity.isDeleted()) {
        return SyncEntityState.DELETED;
      }
      else {
        return SyncEntityState.UPDATED; // TODO: check if entity really has been updated, e.g. by saving last update timestamp on LookupKey row
      }
    }
  }

  protected void setSyncEntityLocalValuesFromLookupKey(SyncEntity entity, SyncEntityLocalLookupKeys lookupKey) {
    if(lookupKey != null) {
      entity.setLocalLookupKey(lookupKey.getEntityLocalLookupKey());
      entity.setLastModifiedOnDevice(lookupKey.getEntityLastModifiedOnDevice());
    }
  }

  protected void entitySuccessfullySynchronized(SyncJobItem jobItem, SyncEntityLocalLookupKeys lookupKey, SyncEntityState syncEntityState,
                                                SyncModuleConfiguration syncModuleConfiguration, Map<String, SyncEntityLocalLookupKeys> allLookupKeys) {
    handleLookupKeyForSuccessfullySynchronizedEntity(jobItem, lookupKey, syncEntityState, syncModuleConfiguration, allLookupKeys);

    jobItem.setState(SyncState.DONE);
    jobItem.setFinishTime(new Date());

    entityManager.updateEntity(jobItem);

    log.info("Successfully synchronized " + jobItem);
  }

  protected void handleLookupKeyForSuccessfullySynchronizedEntity(SyncJobItem jobItem, SyncEntityLocalLookupKeys lookupKey, SyncEntityState syncEntityState,
                                                                  SyncModuleConfiguration syncModuleConfiguration, Map<String, SyncEntityLocalLookupKeys> allLookupKeys) {
    if(syncEntityState == SyncEntityState.DELETED) {
      deleteEntityLookupKey(lookupKey);
      deleteSyncEntityPropertiesLookupKey(jobItem.getEntity(), allLookupKeys);
    }
    else {
      updateEntityLookupKeys(jobItem, lookupKey, syncModuleConfiguration, allLookupKeys);
    }
  }

  private void updateEntityLookupKeys(SyncJobItem jobItem, SyncEntityLocalLookupKeys lookupKey, SyncModuleConfiguration syncModuleConfiguration, Map<String, SyncEntityLocalLookupKeys> allLookupKeys) {
    SyncEntity entity = jobItem.getEntity();

    updateEntityLookupKey(entity, lookupKey);

    updateSyncEntityPropertiesLookupKeys(entity, syncModuleConfiguration, allLookupKeys);
  }

  protected void updateEntityLookupKey(SyncEntity entity, SyncEntityLocalLookupKeys lookupKey) {
    lookupKey.setEntityLocalLookupKey(entity.getLocalLookupKey());
    lookupKey.setEntityLastModifiedOnDevice(entity.getLastModifiedOnDevice());

    entityManager.updateEntity(lookupKey);
  }

  protected void updateSyncEntityPropertiesLookupKeys(SyncEntity entity, SyncModuleConfiguration syncModuleConfiguration, Map<String, SyncEntityLocalLookupKeys> allLookupKeys) {
    // TODO: deleted properties currently aren't detected!
    if(entity instanceof ContactSyncEntity) {
      updateContactSyncEntityPropertiesLookupKeys((ContactSyncEntity)entity, syncModuleConfiguration, allLookupKeys);
    }
  }

  protected void updateContactSyncEntityPropertiesLookupKeys(ContactSyncEntity contact, SyncModuleConfiguration syncModuleConfiguration, Map<String, SyncEntityLocalLookupKeys> allLookupKeys) {
    for(PhoneNumberSyncEntity phoneNumber : contact.getPhoneNumbers()) {
      persistOrUpdateEntityLookupKey(phoneNumber, syncModuleConfiguration, allLookupKeys);
    }

    for(EmailSyncEntity email : contact.getEmailAddresses()) {
      persistOrUpdateEntityLookupKey(email, syncModuleConfiguration, allLookupKeys);
    }
  }

  protected void persistOrUpdateEntityLookupKey(SyncEntity entity, SyncModuleConfiguration syncModuleConfiguration, Map<String, SyncEntityLocalLookupKeys> allLookupKeys) {
    SyncEntityLocalLookupKeys lookupKey = allLookupKeys.get(entity.getId());

    if(lookupKey == null) {
      persistEntryLookupKey(syncModuleConfiguration, entity);
    }
    else {
      updateEntityLookupKey(entity, lookupKey);
    }
  }


  protected void deleteSyncEntityPropertiesLookupKey(SyncEntity entity, Map<String, SyncEntityLocalLookupKeys> allLookupKeys) {
    if(entity instanceof ContactSyncEntity) {
      deleteContactSyncEntityPropertiesLookupKey((ContactSyncEntity)entity, allLookupKeys);
    }
  }

  protected void deleteContactSyncEntityPropertiesLookupKey(ContactSyncEntity contact, Map<String, SyncEntityLocalLookupKeys> allLookupKeys) {
    for(PhoneNumberSyncEntity phoneNumber : contact.getPhoneNumbers()) {
      SyncEntityLocalLookupKeys lookupKey = allLookupKeys.get(phoneNumber.getId());
      if(lookupKey != null) {
        deleteEntityLookupKey(lookupKey);
      }
    }

    for(EmailSyncEntity email : contact.getEmailAddresses()) {
      SyncEntityLocalLookupKeys lookupKey = allLookupKeys.get(email.getId());
      if(lookupKey != null) {
        deleteEntityLookupKey(lookupKey);
      }
    }
  }


  protected Map<String, SyncEntityLocalLookupKeys> getLookupKeysForSyncModuleConfigurationByDatabaseId(SyncModuleConfiguration syncModuleConfiguration) {
    Map<String, SyncEntityLocalLookupKeys> syncModuleConfigurationLookupKeys = new HashMap<>();

    List<SyncEntityLocalLookupKeys> allLookupKeys = entityManager.getAllEntitiesOfType(SyncEntityLocalLookupKeys.class);

    for(SyncEntityLocalLookupKeys lookupKey : allLookupKeys) {
      if(syncModuleConfiguration == lookupKey.getSyncModuleConfiguration()) {
        if(lookupKey.getEntityDatabaseId() != null) {
          syncModuleConfigurationLookupKeys.put(lookupKey.getEntityDatabaseId(), lookupKey);
        }
      }
    }

    return syncModuleConfigurationLookupKeys;
  }


  protected void remoteRetrievedOurFileSyncJobItem(SyncJobItem syncJobItem) {
    FileSyncEntity fileSyncEntity = (FileSyncEntity)syncJobItem.getEntity();
    DiscoveredDevice remoteDevice = syncConfigurationManager.getDiscoveredDeviceForDevice(syncJobItem.getDestinationDevice());

    if(remoteDevice != null) {
      FileSyncJobItem fileSyncJobItem = new FileSyncJobItem(syncJobItem, fileSyncEntity.getFilePath(), remoteDevice.getAddress(),
          FileSyncServiceDefaultConfig.FILE_SYNC_SERVICE_DEFAULT_LISTENER_PORT); // TODO: get actual remote's file service port

      fileSender.sendFileAsync(fileSyncJobItem);
    }
  }


  protected SyncEntityLocalLookupKeys persistEntryLookupKey(SyncModuleConfiguration syncModuleConfiguration, SyncEntity entity) {
    return persistenceHandler.persistEntryLookupKey(syncModuleConfiguration, entity);
  }

  protected void deleteEntityLookupKey(SyncEntityLocalLookupKeys lookupKey) {
    persistenceHandler.deleteEntityLookupKey(lookupKey);
  }


  public List<SyncEntity> getCurrentlySynchronizedEntities() {
    return syncEntitiesCurrentlyBeingSynchronized;
  }

}
