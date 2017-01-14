package net.dankito.sync.synchronization.util;

import net.dankito.sync.SyncConfiguration;
import net.dankito.sync.data.IDataManager;
import net.dankito.sync.devices.DiscoveredDevice;
import net.dankito.sync.devices.IDevicesManager;
import net.dankito.sync.persistence.IEntityManager;
import net.dankito.sync.synchronization.ISyncManager;
import net.dankito.sync.synchronization.SyncConfigurationManagerBase;
import net.dankito.sync.synchronization.modules.ISyncModule;
import net.dankito.utils.IThreadPool;
import net.dankito.utils.services.IFileStorageService;

import java.util.HashMap;
import java.util.Map;


public class SyncConfigurationManagerStub extends SyncConfigurationManagerBase {

  protected Map<String, ISyncModule> mockedAvailableSyncModules;


  public SyncConfigurationManagerStub(ISyncManager syncManager, IDataManager dataManager, IEntityManager entityManager, IDevicesManager devicesManager,
                                      IFileStorageService fileStorageService, IThreadPool threadPool) {
    this(syncManager, dataManager, entityManager, devicesManager, fileStorageService, threadPool, new HashMap<String, ISyncModule>());
  }

  public SyncConfigurationManagerStub(ISyncManager syncManager, IDataManager dataManager, IEntityManager entityManager, IDevicesManager devicesManager,
                                      IFileStorageService fileStorageService, IThreadPool threadPool, Map<String, ISyncModule> mockedAvailableSyncModules) {
    super(syncManager, dataManager, entityManager, devicesManager, fileStorageService, threadPool);
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
