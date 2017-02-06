package net.dankito.sync.synchronization.modules;


import net.dankito.sync.SyncConfiguration;
import net.dankito.sync.devices.DiscoveredDevice;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


public class SyncConfigurationWithDevice {

  protected DiscoveredDevice remoteDevice;

  protected boolean remoteDeviceIsSource;

  protected SyncConfiguration syncConfiguration;

  protected boolean isSyncConfigurationPersisted;

  protected List<SyncModuleSyncModuleConfigurationPair> syncModuleConfigurations = new ArrayList<>();


  public SyncConfigurationWithDevice(DiscoveredDevice remoteDevice, boolean remoteDeviceIsSource, SyncConfiguration syncConfiguration, boolean isSyncConfigurationPersisted) {
    this.remoteDevice = remoteDevice;
    this.remoteDeviceIsSource = remoteDeviceIsSource;
    this.syncConfiguration = syncConfiguration;
    this.isSyncConfigurationPersisted = isSyncConfigurationPersisted;
  }


  public DiscoveredDevice getRemoteDevice() {
    return remoteDevice;
  }

  public boolean getRemoteDeviceIsSource() {
    return remoteDeviceIsSource;
  }

  public SyncConfiguration getSyncConfiguration() {
    return syncConfiguration;
  }

  public boolean isSyncConfigurationPersisted() {
    return isSyncConfigurationPersisted;
  }

  public List<SyncModuleSyncModuleConfigurationPair> getSyncModuleConfigurations() {
    return syncModuleConfigurations;
  }

  public boolean addSyncModuleConfiguration(SyncModuleSyncModuleConfigurationPair pair) {
    return syncModuleConfigurations.add(pair);
  }

  public boolean removeSyncModuleConfiguration(SyncModuleSyncModuleConfigurationPair pair) {
    return syncModuleConfigurations.remove(pair);
  }


  public List<SyncModuleSyncModuleConfigurationPair> getSyncModuleConfigurationsSorted() {
    List<SyncModuleSyncModuleConfigurationPair> sortedSyncConfigurationModules = new ArrayList<>(getSyncModuleConfigurations());

    Collections.sort(sortedSyncConfigurationModules, new Comparator<SyncModuleSyncModuleConfigurationPair>() {
      @Override
      public int compare(SyncModuleSyncModuleConfigurationPair o1, SyncModuleSyncModuleConfigurationPair o2) {
        int displayPriority1 = o1.getSyncModule().getDisplayPriority();
        int displayPriority2 = o2.getSyncModule().getDisplayPriority();

        if(displayPriority1 > displayPriority2) {
          return 1;
        }
        if(displayPriority1 == displayPriority2) {
          return 0;
        }
        return -1;
      }
    });

    return sortedSyncConfigurationModules;
  }

}
