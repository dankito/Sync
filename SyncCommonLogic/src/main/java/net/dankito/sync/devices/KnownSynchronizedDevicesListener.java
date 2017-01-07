package net.dankito.sync.devices;


public interface KnownSynchronizedDevicesListener {

  void knownSynchronizedDeviceConnected(DiscoveredDevice connectedDevice);

  void knownSynchronizedDeviceDisconnected(DiscoveredDevice disconnectedDevice);

}
