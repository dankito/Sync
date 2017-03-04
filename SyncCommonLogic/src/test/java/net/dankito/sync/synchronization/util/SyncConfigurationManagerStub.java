package net.dankito.sync.synchronization.util;

import net.dankito.sync.SyncConfiguration;
import net.dankito.sync.data.IDataManager;
import net.dankito.sync.devices.DiscoveredDevice;
import net.dankito.sync.devices.IDevicesManager;
import net.dankito.sync.persistence.IEntityManager;
import net.dankito.sync.synchronization.ISyncManager;
import net.dankito.sync.synchronization.SyncConfigurationManagerBase;
import net.dankito.sync.synchronization.files.FileSender;
import net.dankito.sync.synchronization.merge.IDataMerger;
import net.dankito.sync.synchronization.modules.ISyncModule;
import net.dankito.utils.IThreadPool;
import net.dankito.utils.services.IFileStorageService;

import java.util.ArrayList;
import java.util.List;


public class SyncConfigurationManagerStub extends SyncConfigurationManagerBase {

  protected List<ISyncModule> mockedAvailableSyncModules;

  protected DiscoveredDevice remoteDevice;


  public SyncConfigurationManagerStub(ISyncManager syncManager, IDataManager dataManager, IEntityManager entityManager, IDevicesManager devicesManager,
                                      IDataMerger dataMerger, FileSender fileSender, IFileStorageService fileStorageService, IThreadPool threadPool, DiscoveredDevice remoteDevice) {
    this(syncManager, dataManager, entityManager, devicesManager, dataMerger, fileSender, fileStorageService, threadPool, new ArrayList<ISyncModule>(), remoteDevice);
  }

  public SyncConfigurationManagerStub(ISyncManager syncManager, IDataManager dataManager, IEntityManager entityManager, IDevicesManager devicesManager,
                                      IDataMerger dataMerger, FileSender fileSender, IFileStorageService fileStorageService, IThreadPool threadPool, List<ISyncModule> mockedAvailableSyncModules, DiscoveredDevice remoteDevice) {
    super(syncManager, dataManager, entityManager, devicesManager, dataMerger, fileSender, fileStorageService, threadPool);
    this.mockedAvailableSyncModules = mockedAvailableSyncModules;
    this.remoteDevice = remoteDevice;

    connectedSynchronizedDevices.add(remoteDevice);
  }


  @Override
  protected List<ISyncModule> retrieveAvailableSyncModules() {
    return mockedAvailableSyncModules;
  }

  public void setMockedAvailableSyncModules(List<ISyncModule> mockedAvailableSyncModules) {
    this.mockedAvailableSyncModules = mockedAvailableSyncModules;
  }

  public void callKnownSynchronizedDeviceConnectedListener() {
    knownSynchronizedDevicesListener.knownSynchronizedDeviceConnected(remoteDevice);
  }

  public void callKnownSynchronizedDeviceDisconnectedListener() {
    knownSynchronizedDevicesListener.knownSynchronizedDeviceDisconnected(remoteDevice);
  }


  public void startContinuousSynchronizationWithDevice(DiscoveredDevice remoteDevice, SyncConfiguration syncConfiguration) {
    super.startContinuousSynchronizationWithDevice(remoteDevice, syncConfiguration);
  }

  @Override
  protected void stopSynchronizingWithDevice(DiscoveredDevice discoveredRemoteDevice, SyncConfiguration syncConfiguration) {
    connectedSynchronizedDevices.remove(discoveredRemoteDevice);

    super.stopSynchronizingWithDevice(discoveredRemoteDevice, syncConfiguration);
  }

  @Override
  protected void remoteDeviceStartedSynchronizingWithUs(SyncConfiguration syncConfiguration) {
    super.remoteDeviceStartedSynchronizingWithUs(syncConfiguration);

    callKnownSynchronizedDeviceConnectedListener();
  }

  @Override
  protected int getDelayBeforePushingEntityChangesToRemote() {
    return 0;
  }

}
