package net.dankito.sync.synchronization;


import android.content.Context;

import net.dankito.sync.ISyncModule;
import net.dankito.sync.devices.IDevicesManager;
import net.dankito.sync.synchronization.modules.AndroidCallLogSyncModule;
import net.dankito.sync.synchronization.modules.AndroidContactsSyncModule;
import net.dankito.sync.synchronization.modules.AndroidPhotosSyncModule;

import java.util.HashMap;
import java.util.Map;

public class SyncConfigurationManagerAndroid extends SyncConfigurationManagerBase {

  protected Context context;


  public SyncConfigurationManagerAndroid(Context context, ISyncManager syncManager, IDevicesManager devicesManager) {
    super(syncManager, devicesManager);

    this.context = context;
  }


  @Override
  protected Map<String, ISyncModule> retrieveAvailableSyncModules() {
    Map<String, ISyncModule> availableSyncModules = new HashMap<>();

    availableSyncModules.put(AndroidPhotosSyncModule.class.getName(), new AndroidPhotosSyncModule(context));
    availableSyncModules.put(AndroidContactsSyncModule.class.getName(), new AndroidContactsSyncModule(context));
    availableSyncModules.put(AndroidCallLogSyncModule.class.getName(), new AndroidCallLogSyncModule(context));

    return availableSyncModules;
  }
}
