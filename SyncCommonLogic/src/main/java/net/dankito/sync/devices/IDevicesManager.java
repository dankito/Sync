package net.dankito.sync.devices;


import net.dankito.sync.Device;
import net.dankito.sync.SyncModuleConfiguration;

import java.util.List;

public interface IDevicesManager {

  void start();

  void stop();


  void startSynchronizingWithDevice(DiscoveredDevice device, List<SyncModuleConfiguration> syncModuleConfigurations);

  void remoteDeviceStartedSynchronizingWithUs(Device remoteDevice);

  void stopSynchronizingWithDevice(DiscoveredDevice device);

  void addDeviceToIgnoreList(DiscoveredDevice device);

  void startSynchronizingWithIgnoredDevice(DiscoveredDevice device, List<SyncModuleConfiguration> syncModuleConfigurations);


  boolean addDiscoveredDevicesListener(DiscoveredDevicesListener listener);

  boolean removeDiscoveredDevicesListener(DiscoveredDevicesListener listener);

  boolean addKnownSynchronizedDevicesListener(KnownSynchronizedDevicesListener listener);

  boolean removeKnownSynchronizedDevicesListener(KnownSynchronizedDevicesListener listener);


  DiscoveredDevice getDiscoveredDeviceForDevice(Device device);

  DiscoveredDevice getDiscoveredDeviceForId(String uniqueDeviceId);

  List<DiscoveredDevice> getAllDiscoveredDevices();

  List<DiscoveredDevice> getKnownSynchronizedDiscoveredDevices();

  List<DiscoveredDevice> getKnownIgnoredDiscoveredDevices();

  List<DiscoveredDevice> getUnknownDiscoveredDevices();
}
