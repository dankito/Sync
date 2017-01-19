package net.dankito.sync.synchronization.modules;


import net.dankito.sync.SyncModuleConfiguration;
import net.dankito.sync.devices.DiscoveredDevice;

import java.util.ArrayList;
import java.util.List;


public class SyncConfigurationChanges {

  protected DiscoveredDevice remoteDevice;

  protected List<SyncModuleConfiguration> addedSyncModuleConfigurations = new ArrayList<>();

  protected List<SyncModuleConfiguration> removedSyncModuleConfigurations = new ArrayList<>();

  protected List<ISyncModule> deactivatedSyncModules = new ArrayList<>();


  public SyncConfigurationChanges(DiscoveredDevice remoteDevice) {
    this.remoteDevice = remoteDevice;
  }


  public DiscoveredDevice getRemoteDevice() {
    return remoteDevice;
  }

  public List<SyncModuleConfiguration> getAddedSyncModuleConfigurations() {
    return addedSyncModuleConfigurations;
  }

  public boolean addAddedSyncModuleConfiguration(SyncModuleConfiguration addedSyncModuleConfiguration) {
    return addedSyncModuleConfigurations.add(addedSyncModuleConfiguration);
  }

  public List<SyncModuleConfiguration> getRemovedSyncModuleConfigurations() {
    return removedSyncModuleConfigurations;
  }

  public boolean addRemovedSyncModuleConfiguration(SyncModuleConfiguration removedSyncModuleConfiguration) {
    return removedSyncModuleConfigurations.add(removedSyncModuleConfiguration);
  }

  public List<ISyncModule> getDeactivatedSyncModules() {
    return deactivatedSyncModules;
  }

  public boolean addDeactivatedSyncModule(ISyncModule deactivatedSyncModule) {
    return deactivatedSyncModules.add(deactivatedSyncModule);
  }

}
