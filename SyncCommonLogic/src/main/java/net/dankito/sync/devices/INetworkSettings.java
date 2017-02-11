package net.dankito.sync.devices;

import net.dankito.sync.Device;
import net.dankito.sync.LocalConfig;

/**
 * Created by ganymed on 15/09/16.
 */
public interface INetworkSettings {

  LocalConfig getLocalConfig();

  Device getLocalHostDevice();

  int getMessagePort();

  void setMessagePort(int messageReceiverPort);

  int getSynchronizationPort();

  void setSynchronizationPort(int synchronizationPort);

  DiscoveredDevice getDiscoveredDevice(String uniqueDeviceId);

  void addDiscoveredDevice(DiscoveredDevice device);

  void removeDiscoveredDevice(DiscoveredDevice device);

  void addConnectedDevicePermittedToSynchronize(DiscoveredDevice device);

  void removeConnectedDevicePermittedToSynchronize(DiscoveredDevice device);

  void addListener(NetworkSettingsChangedListener listener);

  void removeListener(NetworkSettingsChangedListener listener);

}
