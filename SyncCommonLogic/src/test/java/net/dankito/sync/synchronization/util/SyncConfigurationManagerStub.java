package net.dankito.sync.synchronization.util;

import net.dankito.sync.SyncConfiguration;
import net.dankito.sync.data.IDataManager;
import net.dankito.sync.devices.DiscoveredDevice;
import net.dankito.sync.devices.IDevicesManager;
import net.dankito.sync.persistence.IEntityManager;
import net.dankito.sync.synchronization.ISyncManager;
import net.dankito.sync.synchronization.SyncConfigurationManagerBase;
import net.dankito.sync.synchronization.merge.IDataMerger;
import net.dankito.sync.synchronization.modules.ISyncModule;
import net.dankito.utils.IThreadPool;
import net.dankito.utils.services.IFileStorageService;

import java.util.ArrayList;
import java.util.List;


public class SyncConfigurationManagerStub extends SyncConfigurationManagerBase {

  protected List<ISyncModule> mockedAvailableSyncModules;


  public SyncConfigurationManagerStub(ISyncManager syncManager, IDataManager dataManager, IEntityManager entityManager, IDevicesManager devicesManager,
                                      IDataMerger dataMerger, IFileStorageService fileStorageService, IThreadPool threadPool) {
    this(syncManager, dataManager, entityManager, devicesManager, dataMerger, fileStorageService, threadPool, new ArrayList<ISyncModule>());
  }

  public SyncConfigurationManagerStub(ISyncManager syncManager, IDataManager dataManager, IEntityManager entityManager, IDevicesManager devicesManager,
                                      IDataMerger dataMerger, IFileStorageService fileStorageService, IThreadPool threadPool, List<ISyncModule> mockedAvailableSyncModules) {
    super(syncManager, dataManager, entityManager, devicesManager, dataMerger, fileStorageService, threadPool);
    this.mockedAvailableSyncModules = mockedAvailableSyncModules;
  }


  @Override
  protected List<ISyncModule> retrieveAvailableSyncModules() {
    return mockedAvailableSyncModules;
  }

  public void setMockedAvailableSyncModules(List<ISyncModule> mockedAvailableSyncModules) {
    this.mockedAvailableSyncModules = mockedAvailableSyncModules;
  }


  public void startContinuousSynchronizationWithDevice(DiscoveredDevice remoteDevice, SyncConfiguration syncConfiguration) {
    super.startContinuousSynchronizationWithDevice(remoteDevice, syncConfiguration);
  }

  @Override
  protected int getDelayBeforePushingEntityChangesToRemote() {
    return 0;
  }

}
