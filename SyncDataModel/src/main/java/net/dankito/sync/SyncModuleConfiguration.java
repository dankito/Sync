package net.dankito.sync;

import net.dankito.sync.config.DatabaseTableConfig;

import javax.persistence.Column;
import javax.persistence.Entity;


@Entity(name = DatabaseTableConfig.SYNC_MODULE_CONFIGURATION_TABLE_NAME)
public class SyncModuleConfiguration extends BaseEntity {

  @Column(name = DatabaseTableConfig.SYNC_MODULE_CONFIGURATION_SYNC_MODULE_TYPE_COLUMN_NAME)
  protected String syncModuleType;

  @Column(name = DatabaseTableConfig.SYNC_MODULE_CONFIGURATION_IS_BI_DIRECTIONAL_COLUMN_NAME)
  protected boolean isBiDirectional = true;

  @Column(name = DatabaseTableConfig.SYNC_MODULE_CONFIGURATION_SOURCE_PATH_COLUMN_NAME)
  protected String sourcePath; // for FileSyncEntities

  @Column(name = DatabaseTableConfig.SYNC_MODULE_CONFIGURATION_DESTINATION_PATH_COLUMN_NAME)
  protected String destinationPath; // for FileSyncEntities

  @Column(name = DatabaseTableConfig.SYNC_MODULE_CONFIGURATION_KEEP_DELETED_ENTITIES_ON_DESTINATION_COLUMN_NAME)
  protected boolean keepDeletedEntitiesOnDestination = false;


  protected SyncModuleConfiguration() {

  }

  public SyncModuleConfiguration(String syncModuleType) {
    this.syncModuleType = syncModuleType;
  }


  public String getSyncModuleType() {
    return syncModuleType;
  }

  public boolean isBiDirectional() {
    return isBiDirectional;
  }

  public void setBiDirectional(boolean biDirectional) {
    isBiDirectional = biDirectional;
  }

  public String getSourcePath() {
    return sourcePath;
  }

  public void setSourcePath(String sourcePath) {
    this.sourcePath = sourcePath;
  }

  public String getDestinationPath() {
    return destinationPath;
  }

  public void setDestinationPath(String destinationPath) {
    this.destinationPath = destinationPath;
  }

  public boolean isKeepDeletedEntitiesOnDestination() {
    return keepDeletedEntitiesOnDestination;
  }

  public void setKeepDeletedEntitiesOnDestination(boolean keepDeletedEntitiesOnDestination) {
    this.keepDeletedEntitiesOnDestination = keepDeletedEntitiesOnDestination;
  }

}
