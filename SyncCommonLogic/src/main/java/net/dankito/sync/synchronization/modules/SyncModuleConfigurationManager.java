package net.dankito.sync.synchronization.modules;

import net.dankito.sync.Device;
import net.dankito.sync.SyncConfiguration;
import net.dankito.sync.SyncModuleConfiguration;
import net.dankito.sync.data.IDataManager;
import net.dankito.sync.devices.DiscoveredDevice;
import net.dankito.sync.persistence.IEntityManager;
import net.dankito.sync.synchronization.ISyncConfigurationManager;

import java.util.concurrent.atomic.AtomicBoolean;

import javax.inject.Named;


@Named
public class SyncModuleConfigurationManager implements ISyncModuleConfigurationManager {


  protected ISyncConfigurationManager syncConfigurationManager;

  protected IEntityManager entityManager;

  protected Device localDevice;


  public SyncModuleConfigurationManager(ISyncConfigurationManager syncConfigurationManager, IEntityManager entityManager, IDataManager dataManager) {
    this.syncConfigurationManager = syncConfigurationManager;
    this.entityManager = entityManager;
    this.localDevice = dataManager.getLocalConfig().getLocalDevice();
  }


  public SyncConfigurationWithDevice getSyncModuleConfigurationsForDevice(DiscoveredDevice remoteDevice) {
    AtomicBoolean remoteDeviceIsSource = new AtomicBoolean();
    SyncConfiguration persistedSyncConfiguration = getPersistedSyncConfigurationForDevice(remoteDevice, remoteDeviceIsSource);

    if(persistedSyncConfiguration != null) {
      SyncConfigurationWithDevice syncConfigurationWithDevice = new SyncConfigurationWithDevice(remoteDevice, remoteDeviceIsSource.get(), persistedSyncConfiguration, true);
      addSyncModuleConfigurations(syncConfigurationWithDevice, persistedSyncConfiguration);
      return syncConfigurationWithDevice;
    }
    else {
      return createDefaultConfiguration(remoteDevice);
    }
  }


  @Override
  public SyncConfigurationChanges updateSyncConfiguration(SyncConfigurationWithDevice syncModuleConfigurationsForDevice) {
    SyncConfiguration updatedSyncConfiguration = syncModuleConfigurationsForDevice.getSyncConfiguration();
    SyncConfigurationChanges changes = new SyncConfigurationChanges(syncModuleConfigurationsForDevice.getRemoteDevice());

    for(SyncModuleSyncModuleConfigurationPair pair : syncModuleConfigurationsForDevice.getSyncModuleConfigurations()) {
      updateSyncModuleConfiguration(pair, changes);
    }

    entityManager.updateEntity(updatedSyncConfiguration);

    return changes;
  }

  protected void updateSyncModuleConfiguration(SyncModuleSyncModuleConfigurationPair pair, SyncConfigurationChanges changes) {
    SyncModuleConfiguration syncModuleConfiguration = pair.getSyncModuleConfiguration();
    syncModuleConfiguration.setEnabled(pair.isEnabled());
    syncModuleConfiguration.setBidirectional(pair.isBidirectional());
    boolean moduleAddedOrRemoved = false;

    if(pair.isEnabled() == true && pair.originalIsEnabled() == false) {
      changes.addActivatedSyncModuleConfiguration(syncModuleConfiguration);
      moduleAddedOrRemoved = true;
    }
    else if(pair.isEnabled() == false && pair.originalIsEnabled() == true) {
      changes.addDeactivatedSyncModule(pair.getSyncModule());
      moduleAddedOrRemoved = true;
    }

    if(moduleAddedOrRemoved) {
      entityManager.updateEntity(syncModuleConfiguration);
    }
    else if(moduleAddedOrRemoved == false && pair.didConfigurationChange()) {
      if(entityManager.updateEntity(syncModuleConfiguration)) {
        changes.addUpdatedSyncModuleConfiguration(syncModuleConfiguration);
      }
    }
  }


  protected SyncConfiguration getPersistedSyncConfigurationForDevice(DiscoveredDevice remoteDevice, AtomicBoolean remoteDeviceIsSource) {
    SyncConfiguration persistedSyncConfiguration = null;

    for(SyncConfiguration sourceSyncConfiguration : remoteDevice.getDevice().getSourceSyncConfigurations()) {
      if(localDevice == sourceSyncConfiguration.getSourceDevice()) {
        persistedSyncConfiguration = sourceSyncConfiguration;
        remoteDeviceIsSource.set(false);
        break;
      }
    }

    if(persistedSyncConfiguration == null) {
      for(SyncConfiguration destinationSyncConfiguration : remoteDevice.getDevice().getDestinationSyncConfigurations()) {
        if(localDevice == destinationSyncConfiguration.getSourceDevice()) {
          persistedSyncConfiguration = destinationSyncConfiguration;
          remoteDeviceIsSource.set(true);
          break;
        }
      }
    }

    return persistedSyncConfiguration;
  }


  protected void addSyncModuleConfigurations(SyncConfigurationWithDevice syncConfigurationWithDevice, SyncConfiguration syncConfiguration) {
    for(ISyncModule availableSyncModule : syncConfigurationManager.getAvailableSyncModules()) {
      SyncModuleConfiguration syncModuleConfigurationToSyncModule = null;

      for(SyncModuleConfiguration syncModuleConfiguration : syncConfiguration.getSyncModuleConfigurations()) {
        ISyncModule syncModule = syncConfigurationManager.getSyncModuleForSyncModuleConfiguration(syncModuleConfiguration);
        if(syncModule == availableSyncModule) {
          syncModuleConfigurationToSyncModule = syncModuleConfiguration;
          break;
        }
      }

      if(syncModuleConfigurationToSyncModule == null) { // a new SyncModule added
        syncModuleConfigurationToSyncModule = createDefaultSyncModuleConfiguration(availableSyncModule, availableSyncModule.getSyncEntityTypeItCanHandle());
      }

      addSyncModuleConfiguration(syncConfigurationWithDevice, availableSyncModule, syncModuleConfigurationToSyncModule);
    }
  }

  protected void addSyncModuleConfiguration(SyncConfigurationWithDevice syncConfigurationWithDevice, ISyncModule syncModule, SyncModuleConfiguration syncModuleConfiguration) {
    syncConfigurationWithDevice.addSyncModuleConfiguration(new SyncModuleSyncModuleConfigurationPair(syncModule, syncModuleConfiguration, syncModuleConfiguration.isEnabled()));
  }

  protected SyncConfigurationWithDevice createDefaultConfiguration(DiscoveredDevice remoteDevice) {
    SyncConfiguration syncConfiguration = new SyncConfiguration(localDevice, remoteDevice.getDevice());
    SyncConfigurationWithDevice syncConfigurationWithDevice = new SyncConfigurationWithDevice(remoteDevice, false, syncConfiguration, false);

    for(ISyncModule syncModule : syncConfigurationManager.getAvailableSyncModules()) {
      SyncModuleConfiguration syncModuleConfiguration = createDefaultSyncModuleConfiguration(syncModule, syncModule.getSyncEntityTypeItCanHandle());

      syncConfigurationWithDevice.addSyncModuleConfiguration(new SyncModuleSyncModuleConfigurationPair(syncModule, syncModuleConfiguration, true));
    }

    return syncConfigurationWithDevice;
  }

  protected SyncModuleConfiguration createDefaultSyncModuleConfiguration(ISyncModule syncModule, String syncEntityType) {
    SyncModuleConfiguration syncModuleConfiguration = new SyncModuleConfiguration(syncEntityType);

    if(syncModule instanceof IFileSyncModule) {
      setSyncModuleConfigurationForFileSyncModule(syncModuleConfiguration, (IFileSyncModule) syncModule);
    }

    return syncModuleConfiguration;
  }

  protected void setSyncModuleConfigurationForFileSyncModule(SyncModuleConfiguration syncModuleConfiguration, IFileSyncModule syncModule) {
    syncModuleConfiguration.setSourcePath(syncModule.getRootFolder());
    syncModuleConfiguration.setBidirectional(false);
    syncModuleConfiguration.setKeepDeletedEntitiesOnDestination(true);
  }

}
