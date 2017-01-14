package net.dankito.sync.synchronization.helper;


import net.dankito.sync.Device;
import net.dankito.sync.LocalConfig;
import net.dankito.sync.data.IDataManager;
import net.dankito.sync.devices.DevicesManager;
import net.dankito.sync.devices.DiscoveredDevice;
import net.dankito.sync.devices.DiscoveredDeviceType;

public class TestDevicesManager extends DevicesManager {


  public TestDevicesManager() {
    super(null, new IDataManager() {
      @Override
      public LocalConfig getLocalConfig() {
        return new LocalConfig(null);
      }

      @Override
      public Device getLocalDevice() {
        return null;
      }
    }, null);
  }


  public void simulateDeviceConnected(DiscoveredDevice device) {
    callDiscoveredDeviceConnectedListeners(device, DiscoveredDeviceType.UNKNOWN_DEVICE);
  }

  public void simulateDeviceDisconnected(DiscoveredDevice device) {
    callDiscoveredDeviceDisconnectedListeners(device);
  }


  public void simulateKnownSynchronizedDeviceConnected(DiscoveredDevice device) {
    callDiscoveredDeviceConnectedListeners(device, DiscoveredDeviceType.KNOWN_SYNCHRONIZED_DEVICE);
  }

  public void simulateKnownSynchronizedDeviceDisconnected(DiscoveredDevice device) {
    callDiscoveredDeviceDisconnectedListeners(device);
  }

}
