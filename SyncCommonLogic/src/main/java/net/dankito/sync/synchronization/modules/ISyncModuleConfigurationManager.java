package net.dankito.sync.synchronization.modules;


import net.dankito.sync.devices.DiscoveredDevice;


public interface ISyncModuleConfigurationManager {

  SyncConfigurationWithDevice getSyncModuleConfigurationsForDevice(DiscoveredDevice remoteDevice);

}
