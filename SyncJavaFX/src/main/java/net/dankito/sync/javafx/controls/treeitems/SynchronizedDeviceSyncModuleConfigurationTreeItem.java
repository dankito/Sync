package net.dankito.sync.javafx.controls.treeitems;

import net.dankito.sync.devices.DiscoveredDevice;
import net.dankito.sync.synchronization.modules.SyncModuleSyncModuleConfigurationPair;


public class SynchronizedDeviceSyncModuleConfigurationTreeItem extends DeviceTreeItem {


  protected SyncModuleSyncModuleConfigurationPair SyncModuleSyncModuleConfigurationPair;


  public SynchronizedDeviceSyncModuleConfigurationTreeItem(DiscoveredDevice device, SyncModuleSyncModuleConfigurationPair pair) {
    super(device);
    this.SyncModuleSyncModuleConfigurationPair = pair;
  }


  public SyncModuleSyncModuleConfigurationPair getPair() {
    return SyncModuleSyncModuleConfigurationPair;
  }

}
