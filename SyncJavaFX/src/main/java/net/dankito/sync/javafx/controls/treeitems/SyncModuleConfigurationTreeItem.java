package net.dankito.sync.javafx.controls.treeitems;

import net.dankito.sync.SyncModuleConfiguration;

import javafx.scene.control.TreeItem;


public class SyncModuleConfigurationTreeItem extends TreeItem<SyncModuleConfiguration> {


  protected SyncModuleConfiguration syncModuleConfiguration;


  public SyncModuleConfigurationTreeItem(SyncModuleConfiguration syncModuleConfiguration) {
    super(syncModuleConfiguration);
    this.syncModuleConfiguration = syncModuleConfiguration;
  }

}
