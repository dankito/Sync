package net.dankito.sync.devices;


public interface IDevicesManager {

  void start();

  void stop();

  boolean addDiscoveredDevicesListener(DiscoveredDevicesListener listener);

  boolean removeDiscoveredDevicesListener(DiscoveredDevicesListener listener);

  boolean addKnownSynchronizedDevicesListener(KnownSynchronizedDevicesListener listener);

  boolean removeKnownSynchronizedDevicesListener(KnownSynchronizedDevicesListener listener);

}
