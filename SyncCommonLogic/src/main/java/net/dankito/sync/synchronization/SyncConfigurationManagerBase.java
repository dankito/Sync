package net.dankito.sync.synchronization;


import net.dankito.sync.ISyncModule;
import net.dankito.sync.devices.IDevicesManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public abstract class SyncConfigurationManagerBase implements ISyncConfigurationManager {

  protected ISyncManager syncManager;

  protected Map<String, ISyncModule> availableSyncModules = null;


  public SyncConfigurationManagerBase(ISyncManager syncManager, IDevicesManager devicesManager) {
    this.syncManager = syncManager;
  }


  public List<ISyncModule> getAvailableSyncModules() {
    if(availableSyncModules == null) {
      availableSyncModules = retrieveAvailableSyncModules();
    }

    return new ArrayList<>(availableSyncModules.values());
  }

  protected abstract Map<String, ISyncModule> retrieveAvailableSyncModules();

}
