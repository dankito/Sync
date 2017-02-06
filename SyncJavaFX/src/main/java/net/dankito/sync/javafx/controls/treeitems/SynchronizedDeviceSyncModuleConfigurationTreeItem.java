package net.dankito.sync.javafx.controls.treeitems;

import net.dankito.sync.devices.DiscoveredDevice;
import net.dankito.sync.synchronization.modules.SyncModuleSyncModuleConfigurationPair;

import javafx.scene.control.TreeItem;


public class SynchronizedDeviceSyncModuleConfigurationTreeItem extends TreeItem<SyncModuleSyncModuleConfigurationPair> {


  protected DiscoveredDevice device;

  protected SyncModuleSyncModuleConfigurationPair SyncModuleSyncModuleConfigurationPair;


  public SynchronizedDeviceSyncModuleConfigurationTreeItem(DiscoveredDevice device, SyncModuleSyncModuleConfigurationPair pair) {
    super(pair);
    this.device = device;
    this.SyncModuleSyncModuleConfigurationPair = pair;
  }


  public DiscoveredDevice getDevice() {
    return device;
  }

}
