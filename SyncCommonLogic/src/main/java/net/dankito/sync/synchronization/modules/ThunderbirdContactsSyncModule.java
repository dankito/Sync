package net.dankito.sync.synchronization.modules;

import net.dankito.sync.ContactSyncEntity;
import net.dankito.sync.SyncEntityState;
import net.dankito.sync.SyncJobItem;
import net.dankito.sync.devices.DiscoveredDevice;
import net.dankito.sync.localization.Localization;
import net.dankito.sync.synchronization.SyncEntityChange;
import net.dankito.sync.synchronization.SyncEntityChangeListener;
import net.dankito.sync.thunderbird.ThunderbirdPluginConnector;
import net.dankito.sync.thunderbird.callback.ThunderbirdCallback;
import net.dankito.sync.thunderbird.response.GetAddressBookResponse;
import net.dankito.utils.IThreadPool;

import java.util.ArrayList;


public class ThunderbirdContactsSyncModule extends SyncModuleBase {

  protected DiscoveredDevice thunderbird;

  protected ThunderbirdPluginConnector connector;


  public ThunderbirdContactsSyncModule(DiscoveredDevice thunderbird, Localization localization, IThreadPool threadPool) {
    super(localization);

    this.thunderbird = thunderbird;
    this.connector = new ThunderbirdPluginConnector(thunderbird, threadPool, thunderbirdEntityChangeListener);
  }

  @Override
  protected String getNameStringResourceKey() {
    return "sync.module.name.contacts";
  }

  @Override
  public int getDisplayPriority() {
    return DISPLAY_PRIORITY_HIGH;
  }

  @Override
  public String getSyncEntityTypeItCanHandle() {
    return SyncModuleDefaultTypes.CONTACTS.getTypeName();
  }

  @Override
  public void readAllEntitiesAsync(final ReadEntitiesCallback callback) {
    connector.getAddressBookAsync(new ThunderbirdCallback<GetAddressBookResponse>() {
      @Override
      public void done(GetAddressBookResponse response) {
        if(response.isCouldHandleMessage()) {
          callback.done(true, response.getBody());
        }
        else {
          callback.done(false, new ArrayList<ContactSyncEntity>());
        }
      }
    });

    super.readAllEntitiesAsync(callback);
  }

  @Override
  public void handleRetrievedSynchronizedEntityAsync(SyncJobItem jobItem, SyncEntityState entityState, HandleRetrievedSynchronizedEntityCallback callback) {
    if(jobItem.getEntity() instanceof ContactSyncEntity) { // should actually always be the case, just to make sure
      if(jobItem.getSourceDevice() != thunderbird.getDevice()) {
        connector.syncContact((ContactSyncEntity) jobItem.getEntity(), entityState);
      }
    }
  }


  protected SyncEntityChangeListener thunderbirdEntityChangeListener = new SyncEntityChangeListener() {
    @Override
    public void entityChanged(SyncEntityChange change) {
      callSyncEntityChangeListeners(new SyncEntityChange(ThunderbirdContactsSyncModule.this, change.getSyncEntity(), change.getState(), thunderbird));
    }
  };

}
