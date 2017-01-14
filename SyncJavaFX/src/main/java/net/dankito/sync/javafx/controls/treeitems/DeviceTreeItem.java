package net.dankito.sync.javafx.controls.treeitems;

import net.dankito.sync.Device;
import net.dankito.sync.SyncConfiguration;
import net.dankito.sync.SyncModuleConfiguration;
import net.dankito.sync.devices.DiscoveredDevice;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javafx.scene.control.TreeItem;


public class DeviceTreeItem extends TreeItem<DiscoveredDevice> {

  protected DiscoveredDevice remoteDevice;

  protected Device localDevice;



  public DeviceTreeItem(DiscoveredDevice remoteDevice, Device localDevice) {
    super(remoteDevice);
    this.remoteDevice = remoteDevice;
    this.localDevice = localDevice;

    setupItem(remoteDevice);
  }


  protected void setupItem(DiscoveredDevice device) {
    List<SyncConfiguration> syncConfigurations = getSyncConfigurationsForAllSynchronizingDevices(device.getDevice());

    for(SyncConfiguration syncConfiguration : syncConfigurations) {
      for(SyncModuleConfiguration syncModuleConfiguration : syncConfiguration.getSyncModuleConfigurations()) {

      }
    }
  }

  private List<SyncConfiguration> getSyncConfigurationsForAllSynchronizingDevices(Device remoteDevice) {
    List<SyncConfiguration> syncConfigurations = new ArrayList<>();

    for(SyncConfiguration syncConfiguration : remoteDevice.getSourceSyncConfigurations()) {
      if(syncConfiguration.getDestinationDevice() == localDevice) {
        syncConfigurations.add(syncConfiguration);
      }
    }

    for(SyncConfiguration syncConfiguration : remoteDevice.getDestinationSyncConfigurations()) {
      if(syncConfiguration.getSourceDevice() == localDevice) {
        syncConfigurations.add(syncConfiguration);
      }
    }

    Collections.sort(syncConfigurations, new Comparator<SyncConfiguration>() {
      @Override
      public int compare(SyncConfiguration config1, SyncConfiguration config2) {
        Device device1 = config1.getSourceDevice() == remoteDevice ? config1.getDestinationDevice() : config1.getSourceDevice();
        Device device2 = config2.getSourceDevice() == remoteDevice ? config2.getDestinationDevice() : config2.getSourceDevice();

        return device1.toString().compareTo(device2.toString());
      }
    });

    return syncConfigurations;
  }

}
