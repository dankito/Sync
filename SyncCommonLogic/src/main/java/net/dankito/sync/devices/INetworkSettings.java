package net.dankito.sync.devices;

import net.dankito.sync.Device;
import net.dankito.sync.LocalConfig;

/**
 * Created by ganymed on 15/09/16.
 */
public interface INetworkSettings {

  LocalConfig getLocalConfig();

  Device getLocalHostDevice();

  void setLocalHostDevice(Device localDevice);

  int getMessagePort();

  void setMessagePort(int messageReceiverPort);

  int getSynchronizationPort();

  void setSynchronizationPort(int synchronizationPort);

  boolean isDevicePermittedToSynchronize(String uniqueDeviceId);

  void addDevicePermittedToSynchronize(Device device);

  void removeDevicePermittedToSynchronize(Device device);

  void addListener(NetworkSettingsChangedListener listener);

  void removeListener(NetworkSettingsChangedListener listener);

}
