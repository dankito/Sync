package net.dankito.sync.javafx.controls.treeitems;

import net.dankito.sync.synchronization.modules.SyncModuleSyncModuleConfigurationPair;

import javafx.scene.control.TreeItem;


public class SyncModuleConfigurationTreeItem extends TreeItem<SyncModuleSyncModuleConfigurationPair> {


  protected SyncModuleSyncModuleConfigurationPair SyncModuleSyncModuleConfigurationPair;


  public SyncModuleConfigurationTreeItem(SyncModuleSyncModuleConfigurationPair pair) {
    super(pair);
    this.SyncModuleSyncModuleConfigurationPair = pair;
  }

}
