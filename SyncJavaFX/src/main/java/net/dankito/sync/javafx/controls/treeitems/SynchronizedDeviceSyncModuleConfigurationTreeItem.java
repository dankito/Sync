package net.dankito.sync.javafx.controls.treeitems;

import net.dankito.sync.synchronization.modules.SyncModuleSyncModuleConfigurationPair;

import javafx.scene.control.TreeItem;


public class SynchronizedDeviceSyncModuleConfigurationTreeItem extends TreeItem<SyncModuleSyncModuleConfigurationPair> {


  protected SyncModuleSyncModuleConfigurationPair SyncModuleSyncModuleConfigurationPair;


  public SynchronizedDeviceSyncModuleConfigurationTreeItem(SyncModuleSyncModuleConfigurationPair pair) {
    super(pair);
    this.SyncModuleSyncModuleConfigurationPair = pair;
  }

}
