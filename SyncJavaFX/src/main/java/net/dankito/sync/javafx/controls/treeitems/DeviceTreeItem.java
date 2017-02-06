package net.dankito.sync.javafx.controls.treeitems;

import net.dankito.sync.devices.DiscoveredDevice;

import javafx.scene.control.TreeItem;


/**
 * I cannot use generics as it's not possible to add to a TreeItem<DiscoveredDevice> a TreeItem<SyncModuleConfiguration> as child.
 */
public class DeviceTreeItem extends TreeItem {

  protected DiscoveredDevice remoteDevice;



  public DeviceTreeItem(DiscoveredDevice remoteDevice) {
    super(remoteDevice);
    this.remoteDevice = remoteDevice;
  }

}
