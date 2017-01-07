package net.dankito.sync;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ganymed on 07/01/17.
 */

public class SyncConfiguration extends BaseEntity {

  protected Device sourceDevice;

  protected Device destinationDevice;

  protected List<SyncModuleConfiguration> syncModuleConfigurations = new ArrayList<>();


  public SyncConfiguration(Device sourceDevice, Device destinationDevice) {
    this.sourceDevice = sourceDevice;
    this.destinationDevice = destinationDevice;
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
