package net.dankito.sync.synchronization;


import net.dankito.sync.Device;
import net.dankito.sync.ISyncModule;
import net.dankito.sync.LocalConfig;
import net.dankito.sync.ReadEntitiesCallback;
import net.dankito.sync.SyncConfiguration;
import net.dankito.sync.SyncEntity;
import net.dankito.sync.SyncJobItem;
import net.dankito.sync.SyncModuleConfiguration;
import net.dankito.sync.devices.DiscoveredDevice;
import net.dankito.sync.devices.DiscoveredDeviceType;
import net.dankito.sync.devices.DiscoveredDevicesListener;
import net.dankito.sync.devices.IDevicesManager;
import net.dankito.sync.persistence.IEntityManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public abstract class SyncConfigurationManagerBase implements ISyncConfigurationManager {

  protected ISyncManager syncManager;

  protected IEntityManager entityManager;

  protected LocalConfig localConfig;

  protected Map<String, ISyncModule> availableSyncModules = null;

  protected List<DiscoveredDevice> connectedSynchronizedDevices = new ArrayList<>();


  public SyncConfigurationManagerBase(ISyncManager syncManager, IEntityManager entityManager, IDevicesManager devicesManager, LocalConfig localConfig) {
    this.syncManager = syncManager;
    this.entityManager = entityManager;
    this.localConfig = localConfig;

    devicesManager.addDiscoveredDevicesListener(discoveredDevicesListener);
  }


  protected abstract Map<String, ISyncModule> retrieveAvailableSyncModules();


  protected void connectedToSynchronizedDevice(DiscoveredDevice remoteDevice) {
    connectedSynchronizedDevices.add(remoteDevice);

    SyncConfiguration syncConfiguration = getSyncConfigurationForDevice(remoteDevice.getDevice());
    if(syncConfiguration != null) {
      startContinuouslySynchronizationWithDevice(remoteDevice, syncConfiguration);
    }
  }

  protected void startContinuouslySynchronizationWithDevice(DiscoveredDevice remoteDevice, SyncConfiguration syncConfiguration) {
    for(SyncModuleConfiguration syncModuleConfiguration : syncConfiguration.getSyncModuleConfigurations()) {
      startContinuouslySynchronizationForModule(remoteDevice, syncModuleConfiguration);
    }
  }

  protected void startContinuouslySynchronizationForModule(final DiscoveredDevice remoteDevice, final SyncModuleConfiguration syncModuleConfiguration) {
    ISyncModule syncModule = getSyncModuleForClassName(syncModuleConfiguration.getSyncModuleClassName());
    if(syncModule != null) {
      syncModule.readAllEntitiesAsync(syncModuleConfiguration, new ReadEntitiesCallback() {
        @Override
        public void done(List<SyncEntity> entities) {
          List<SyncEntity> entitiesToSync = getEntitiesToSynchronize(remoteDevice, syncModuleConfiguration, entities);
          pushSyncEntitiesToRemote(remoteDevice, syncModuleConfiguration, entitiesToSync);
        }
      });
    }
  }


  protected List<SyncEntity> getEntitiesToSynchronize(DiscoveredDevice remoteDevice, SyncModuleConfiguration syncModuleConfiguration, List<SyncEntity> entities) {
    List<SyncEntity> entitiesToSync = new ArrayList<>();

    // TODO: get last synchronization state

    for(SyncEntity entity : entities) {
      if(entity.getId() == null) {
        entityManager.persistEntity(entity);
      }

      entitiesToSync.add(entity);
    }

    return entitiesToSync;
  }

  protected void pushSyncEntitiesToRemote(DiscoveredDevice remoteDevice, SyncModuleConfiguration syncModuleConfiguration, List<SyncEntity> entities) {
    for(SyncEntity syncEntity : entities) {
      pushSyncEntityToRemote(remoteDevice, syncModuleConfiguration, syncEntity);
    }
  }

  protected void pushSyncEntityToRemote(DiscoveredDevice remoteDevice, SyncModuleConfiguration syncModuleConfiguration, SyncEntity entity) {
    SyncJobItem jobItem = new SyncJobItem(syncModuleConfiguration, entity);
    entityManager.persistEntity(jobItem);
  }


  protected SyncConfiguration getSyncConfigurationForDevice(Device device) {
    SyncConfiguration syncConfiguration = null;
    Device localDevice = localConfig.getLocalDevice();

    for(SyncConfiguration config : device.getDestinationSyncConfigurations()) {
      if(localDevice == config.getSourceDevice()) {
        syncConfiguration = config;
        break;
      }
    }

    return syncConfiguration;
  }

  protected ISyncModule getSyncModuleForClassName(String syncModuleClassName) {
    getAvailableSyncModules(); // ensure availableSyncModules are loaded
    return availableSyncModules.get(syncModuleClassName);
  }


  public List<ISyncModule> getAvailableSyncModules() {
    synchronized(this) {
      if(availableSyncModules == null) {
        availableSyncModules = retrieveAvailableSyncModules();
      }
    }

    return new ArrayList<>(availableSyncModules.values());
  }


  protected DiscoveredDevicesListener discoveredDevicesListener = new DiscoveredDevicesListener() {
    @Override
    public void deviceDiscovered(DiscoveredDevice connectedDevice, DiscoveredDeviceType type) {
      if(type == DiscoveredDeviceType.KNOWN_SYNCHRONIZED_DEVICE) {
        connectedToSynchronizedDevice(connectedDevice);
      }
    }

    @Override
    public void disconnectedFromDevice(DiscoveredDevice disconnectedDevice) {

    }
  };

}
