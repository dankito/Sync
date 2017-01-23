package net.dankito.sync.synchronization.modules;


import net.dankito.sync.SyncModuleConfiguration;
import net.dankito.sync.devices.DiscoveredDevice;

import java.util.ArrayList;
import java.util.List;


public class SyncConfigurationChanges {

  protected DiscoveredDevice remoteDevice;

  protected List<SyncModuleConfiguration> activatedSyncModuleConfigurations = new ArrayList<>();

  protected List<SyncModuleConfiguration> updatedSyncModuleConfigurations = new ArrayList<>();

  protected List<ISyncModule> deactivatedSyncModules = new ArrayList<>();


  public SyncConfigurationChanges(DiscoveredDevice remoteDevice) {
    this.remoteDevice = remoteDevice;
  }


  public DiscoveredDevice getRemoteDevice() {
    return remoteDevice;
  }

  public List<SyncModuleConfiguration> getActivatedSyncModuleConfigurations() {
    return activatedSyncModuleConfigurations;
  }

  public boolean addActivatedSyncModuleConfiguration(SyncModuleConfiguration activatedSyncModuleConfiguration) {
    return activatedSyncModuleConfigurations.add(activatedSyncModuleConfiguration);
  }

  public List<SyncModuleConfiguration> getUpdatedSyncModuleConfigurations() {
    return updatedSyncModuleConfigurations;
  }

  public boolean addUpdatedSyncModuleConfiguration(SyncModuleConfiguration updatedSyncModuleConfiguration) {
    return updatedSyncModuleConfigurations.add(updatedSyncModuleConfiguration);
  }

  public List<ISyncModule> getDeactivatedSyncModules() {
    return deactivatedSyncModules;
  }

  public boolean addDeactivatedSyncModule(ISyncModule deactivatedSyncModule) {
    return deactivatedSyncModules.add(deactivatedSyncModule);
  }

}
