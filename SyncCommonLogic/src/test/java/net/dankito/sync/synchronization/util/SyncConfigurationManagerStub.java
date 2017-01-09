package net.dankito.sync.synchronization.util;

import net.dankito.sync.ISyncModule;
import net.dankito.sync.LocalConfig;
import net.dankito.sync.SyncConfiguration;
import net.dankito.sync.devices.DiscoveredDevice;
import net.dankito.sync.devices.IDevicesManager;
import net.dankito.sync.persistence.IEntityManager;
import net.dankito.sync.synchronization.ISyncManager;
import net.dankito.sync.synchronization.SyncConfigurationManagerBase;

import java.util.HashMap;
import java.util.Map;


public class SyncConfigurationManagerStub extends SyncConfigurationManagerBase {

  protected Map<String, ISyncModule> mockedAvailableSyncModules;


  public SyncConfigurationManagerStub(ISyncManager syncManager, IEntityManager entityManager, IDevicesManager devicesManager, LocalConfig localConfig) {
    this(syncManager, entityManager, devicesManager, localConfig, new HashMap<String, ISyncModule>());
  }

  public SyncConfigurationManagerStub(ISyncManager syncManager, IEntityManager entityManager, IDevicesManager devicesManager, LocalConfig localConfig, Map<String, ISyncModule> mockedAvailableSyncModules) {
    super(syncManager, entityManager, devicesManager, localConfig);
    this.mockedAvailableSyncModules = mockedAvailableSyncModules;
  }


  @Override
  protected Map<String, ISyncModule> retrieveAvailableSyncModules() {
    return mockedAvailableSyncModules;
  }

  public void setMockedAvailableSyncModules(Map<String, ISyncModule> mockedAvailableSyncModules) {
    this.mockedAvailableSyncModules = mockedAvailableSyncModules;
  }


  public void startContinuouslySynchronizationWithDevice(DiscoveredDevice remoteDevice, SyncConfiguration syncConfiguration) {
    super.startContinuouslySynchronizationWithDevice(remoteDevice, syncConfiguration);
  }

}
