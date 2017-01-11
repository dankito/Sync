package net.dankito.sync;

import net.dankito.sync.config.DatabaseTableConfig;

import javax.persistence.Column;
import javax.persistence.Entity;


@Entity(name = DatabaseTableConfig.SYNC_MODULE_CONFIGURATION_TABLE_NAME)
public class SyncModuleConfiguration extends BaseEntity {

  @Column(name = DatabaseTableConfig.SYNC_MODULE_CONFIGURATION_SYNC_MODULE_CLASS_NAME_COLUMN_NAME)
  protected String syncModuleClassName;

  @Column(name = DatabaseTableConfig.SYNC_MODULE_CONFIGURATION_IS_BI_DIRECTIONAL_COLUMN_NAME)
  protected boolean isBiDirectional;

  @Column(name = DatabaseTableConfig.SYNC_MODULE_CONFIGURATION_DESTINATION_PATH_COLUMN_NAME)
  protected String destinationPath; // for FileSyncEntities


  public SyncModuleConfiguration() {

  }

  public SyncModuleConfiguration(String syncModuleClassName) {
    this.syncModuleClassName = syncModuleClassName;
  }


  public String getSyncModuleClassName() {
    return syncModuleClassName;
  }

  public void setSyncModuleClassName(String syncModuleClassName) {
    this.syncModuleClassName = syncModuleClassName;
  }

  public boolean isBiDirectional() {
    return isBiDirectional;
  }

  public void setBiDirectional(boolean biDirectional) {
    isBiDirectional = biDirectional;
  }

  public String getDestinationPath() {
    return destinationPath;
  }

  public void setDestinationPath(String destinationPath) {
    this.destinationPath = destinationPath;
  }

}
