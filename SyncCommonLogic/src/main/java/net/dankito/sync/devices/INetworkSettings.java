package net.dankito.sync.devices;

import net.dankito.sync.Device;

/**
 * Created by ganymed on 15/09/16.
 */
public interface INetworkSettings {

  Device getLocalHostDevice();

  void setLocalHostDevice(Device localDevice);

  int getMessagePort();

  void setMessagePort(int messageReceiverPort);

  int getSynchronizationPort();

  void setSynchronizationPort(int synchronizationPort);

  boolean isDevicePermittedToSynchronize(String uniqueDeviceId);

  void addListener(NetworkSettingsChangedListener listener);

  void removeListener(NetworkSettingsChangedListener listener);

}
