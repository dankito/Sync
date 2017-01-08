package net.dankito.sync.devices;


import java.util.List;

public interface IDevicesManager {

  void start();

  void stop();

  boolean addDiscoveredDevicesListener(DiscoveredDevicesListener listener);

  boolean removeDiscoveredDevicesListener(DiscoveredDevicesListener listener);

  boolean addKnownSynchronizedDevicesListener(KnownSynchronizedDevicesListener listener);

  boolean removeKnownSynchronizedDevicesListener(KnownSynchronizedDevicesListener listener);

  List<DiscoveredDevice> getAllDiscoveredDevices();

  List<DiscoveredDevice> getKnownSynchronizedDiscoveredDevices();

  List<DiscoveredDevice> getKnownIgnoredDiscoveredDevices();

  List<DiscoveredDevice> getUnknownDiscoveredDevices();
}
