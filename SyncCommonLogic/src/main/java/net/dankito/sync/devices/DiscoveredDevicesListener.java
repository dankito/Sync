package net.dankito.sync.devices;


public interface DiscoveredDevicesListener {

  void deviceDiscovered(DiscoveredDevice connectedDevice);

  void disconnectedFromDevice(DiscoveredDevice disconnectedDevice);

}
