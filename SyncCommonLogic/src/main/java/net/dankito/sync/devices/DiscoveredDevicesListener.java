package net.dankito.sync.devices;


public interface DiscoveredDevicesListener {

  void deviceDiscovered(DiscoveredDevice connectedDevice, DiscoveredDeviceType type);

  void disconnectedFromDevice(DiscoveredDevice disconnectedDevice);

}
