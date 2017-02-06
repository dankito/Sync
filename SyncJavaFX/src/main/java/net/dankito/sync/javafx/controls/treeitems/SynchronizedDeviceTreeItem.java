package net.dankito.sync.javafx.controls.treeitems;

import net.dankito.sync.devices.DiscoveredDevice;
import net.dankito.sync.synchronization.modules.ISyncModuleConfigurationManager;
import net.dankito.sync.synchronization.modules.SyncConfigurationWithDevice;
import net.dankito.sync.synchronization.modules.SyncModuleSyncModuleConfigurationPair;


public class SynchronizedDeviceTreeItem extends DeviceTreeItem {

  protected ISyncModuleConfigurationManager syncModuleConfigurationManager;



  public SynchronizedDeviceTreeItem(DiscoveredDevice remoteDevice, ISyncModuleConfigurationManager syncModuleConfigurationManager) {
    super(remoteDevice);

    this.syncModuleConfigurationManager = syncModuleConfigurationManager;

    setupSyncModuleConfigurationChildren(remoteDevice, syncModuleConfigurationManager);
  }


  protected void setupSyncModuleConfigurationChildren(DiscoveredDevice remoteDevice, ISyncModuleConfigurationManager syncModuleConfigurationManager) {
    setExpanded(true);

    SyncConfigurationWithDevice syncModuleConfigurationsForDevice = syncModuleConfigurationManager.getSyncModuleConfigurationsForDevice(remoteDevice);

    for(SyncModuleSyncModuleConfigurationPair pair : syncModuleConfigurationsForDevice.getSyncModuleConfigurationsSorted()) {
      addSyncModuleConfigurationChild(pair);
    }
  }

  protected void addSyncModuleConfigurationChild(SyncModuleSyncModuleConfigurationPair pair) {
    SyncModuleConfigurationTreeItem child = new SyncModuleConfigurationTreeItem(pair);

    getChildren().add(child);
  }

}
