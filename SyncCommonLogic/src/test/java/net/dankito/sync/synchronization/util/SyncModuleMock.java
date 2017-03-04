package net.dankito.sync.synchronization.util;

import net.dankito.sync.ContactSyncEntity;
import net.dankito.sync.EmailSyncEntity;
import net.dankito.sync.PhoneNumberSyncEntity;
import net.dankito.sync.SyncEntity;
import net.dankito.sync.SyncEntityState;
import net.dankito.sync.SyncJobItem;
import net.dankito.sync.SyncModuleConfiguration;
import net.dankito.sync.devices.DiscoveredDevice;
import net.dankito.sync.synchronization.SyncEntityChange;
import net.dankito.sync.synchronization.SyncEntityChangeListener;
import net.dankito.sync.synchronization.modules.HandleRetrievedSynchronizedEntityCallback;
import net.dankito.sync.synchronization.modules.HandleRetrievedSynchronizedEntityResult;
import net.dankito.sync.synchronization.modules.ISyncModule;
import net.dankito.sync.synchronization.modules.ReadEntitiesCallback;
import net.dankito.sync.synchronization.modules.SyncModuleBase;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class SyncModuleMock implements ISyncModule {

  protected static final String TEST_SYNC_MODULE_TYPE = "TestSyncModule";


  protected List<SyncEntity> entitiesToReturnFromReadAllEntitiesAsync;

  protected List<SyncEntityChangeListener> listeners = new ArrayList();


  public SyncModuleMock(List<SyncEntity> entitiesToReturnFromReadAllEntitiesAsync) {
    this.entitiesToReturnFromReadAllEntitiesAsync = entitiesToReturnFromReadAllEntitiesAsync;
  }

  @Override
  public String getName() {
    return "Mock";
  }

  @Override
  public int getDisplayPriority() {
    return SyncModuleBase.DISPLAY_PRIORITY_LOWEST;
  }

  @Override
  public String getSyncEntityTypeItCanHandle() {
    return TEST_SYNC_MODULE_TYPE;
  }

  @Override
  public void readAllEntitiesAsync(ReadEntitiesCallback callback) {
    for(SyncEntity entity : entitiesToReturnFromReadAllEntitiesAsync) {
      setEntityLocalLookupKeyAndLastModifiedOnDevice(entity, false);
    }

    callback.done(new ArrayList<SyncEntity>(entitiesToReturnFromReadAllEntitiesAsync));
  }

  @Override
  public void handleRetrievedSynchronizedEntityAsync(SyncJobItem jobItem, SyncEntityState entityState, HandleRetrievedSynchronizedEntityCallback callback) {
    SyncEntity syncEntity = jobItem.getEntity();
    setEntityLocalLookupKeyAndLastModifiedOnDevice(syncEntity, true);

    callback.done(new HandleRetrievedSynchronizedEntityResult(jobItem, true));
  }

  @Override
  public boolean deleteSyncEntityProperty(SyncEntity entity, SyncEntity property) {
    return false;
  }

  @Override
  public void addSyncEntityChangeListener(SyncEntityChangeListener listener) {
    listeners.add(listener);
  }

  @Override
  public void removeSyncEntityChangeListener(SyncEntityChangeListener listener) {
    listeners.remove(listener);
  }

  @Override
  public void configureLocalSynchronizationSettings(DiscoveredDevice remoteDevice, SyncModuleConfiguration syncModuleConfiguration) {

  }

  public void callEntityChangedListeners(List<SyncEntity> testEntities) {
    entitiesToReturnFromReadAllEntitiesAsync.clear();
    entitiesToReturnFromReadAllEntitiesAsync.addAll(testEntities);

    for(SyncEntityChangeListener listener : listeners) {
      listener.entityChanged(new SyncEntityChange(this, null));
    }
  }


  protected void setEntityLocalLookupKeyAndLastModifiedOnDevice(SyncEntity entity, boolean updateLastModifiedIfNotNull) {
    if(entity.getLocalLookupKey() == null) {
      entity.setLocalLookupKey(entity.getId() + "_lookup_key_mock");
    }

    if(updateLastModifiedIfNotNull || entity.getLastModifiedOnDevice() == null) {
      entity.setLastModifiedOnDevice(new Date());
    }

    setEntitySyncEntityPropertiesLocalLookupKeyAndLastModifiedOnDevice(entity, updateLastModifiedIfNotNull);
  }

  protected void setEntitySyncEntityPropertiesLocalLookupKeyAndLastModifiedOnDevice(SyncEntity entity, boolean updateLastModifiedIfNotNull) {
    if(entity instanceof ContactSyncEntity) {
      ContactSyncEntity contact = (ContactSyncEntity)entity;

      for(PhoneNumberSyncEntity phoneNumber : contact.getPhoneNumbers()) {
        setEntityLocalLookupKeyAndLastModifiedOnDevice(phoneNumber, updateLastModifiedIfNotNull);
      }
      for(EmailSyncEntity email : contact.getEmailAddresses()) {
        setEntityLocalLookupKeyAndLastModifiedOnDevice(email, updateLastModifiedIfNotNull);
      }
    }
  }

}
