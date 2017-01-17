package net.dankito.sync.synchronization;


import net.dankito.sync.SyncEntity;
import net.dankito.sync.SyncModuleConfiguration;
import net.dankito.sync.devices.DiscoveredDevice;

public class EntitiesSyncQueueItem {

  protected SyncEntity entityToPush;

  protected DiscoveredDevice remoteDevice;

  protected SyncModuleConfiguration syncModuleConfiguration;


  public EntitiesSyncQueueItem(SyncEntity entityToPush, DiscoveredDevice remoteDevice, SyncModuleConfiguration syncModuleConfiguration) {
    this.entityToPush = entityToPush;
    this.remoteDevice = remoteDevice;
    this.syncModuleConfiguration = syncModuleConfiguration;
  }


  public SyncEntity getEntityToPush() {
    return entityToPush;
  }

  public DiscoveredDevice getRemoteDevice() {
    return remoteDevice;
  }

  public SyncModuleConfiguration getSyncModuleConfiguration() {
    return syncModuleConfiguration;
  }

}
