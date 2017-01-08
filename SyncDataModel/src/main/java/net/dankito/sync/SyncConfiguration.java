package net.dankito.sync;

import net.dankito.sync.config.DatabaseTableConfig;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;


@Entity(name = DatabaseTableConfig.SYNC_CONFIGURATION_TABLE_NAME)
public class SyncConfiguration extends BaseEntity {

  @ManyToOne
  @JoinColumn(name = DatabaseTableConfig.SYNC_CONFIGURATION_SOURCE_DEVICE_JOIN_COLUMN_NAME)
  protected Device sourceDevice;

  @ManyToOne
  @JoinColumn(name = DatabaseTableConfig.SYNC_CONFIGURATION_DESTINATION_DEVICE_JOIN_COLUMN_NAME)
  protected Device destinationDevice;

  @OneToMany(cascade = { CascadeType.PERSIST })
  protected List<SyncModuleConfiguration> syncModuleConfigurations = new ArrayList<>();


  protected SyncConfiguration() { // for reflection

  }

  public SyncConfiguration(Device sourceDevice, Device destinationDevice) {
    this.sourceDevice = sourceDevice;
    this.destinationDevice = destinationDevice;
  }

  public SyncConfiguration(Device sourceDevice, Device destinationDevice, List<SyncModuleConfiguration> syncModuleConfigurations) {
    this(sourceDevice, destinationDevice);
    this.syncModuleConfigurations = syncModuleConfigurations;
  }


  public Device getSourceDevice() {
    return sourceDevice;
  }

  public Device getDestinationDevice() {
    return destinationDevice;
  }

  public List<SyncModuleConfiguration> getSyncModuleConfigurations() {
    return syncModuleConfigurations;
  }

  public boolean addSyncModuleConfiguration(SyncModuleConfiguration config) {
    return syncModuleConfigurations.add(config);
  }

  public boolean removeSyncModuleConfiguration(SyncModuleConfiguration config) {
    return syncModuleConfigurations.remove(config);
  }

}
