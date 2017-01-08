package net.dankito.sync.synchronization.helper;


import net.dankito.sync.devices.DevicesManager;
import net.dankito.sync.devices.DiscoveredDevice;
import net.dankito.sync.devices.DiscoveredDeviceType;

public class TestDevicesManager extends DevicesManager {


  public TestDevicesManager() {
    super(null, null);
  }


  public void simulateDeviceConnected(DiscoveredDevice device) {
    callDiscoveredDeviceConnectedListeners(device, DiscoveredDeviceType.UNKNOWN_DEVICE);
  }

  public void simulateDeviceDisconnected(DiscoveredDevice device) {
    callDiscoveredDeviceDisconnectedListeners(device);
  }


  public void simulateKnownSynchronizedDeviceConnected(DiscoveredDevice device) {
    callKnownSynchronizedDeviceConnected(device);
  }

  public void simulateKnownSynchronizedDeviceDisconnected(DiscoveredDevice device) {
    callKnownSynchronizedDeviceDisconnected(device);
  }

}
