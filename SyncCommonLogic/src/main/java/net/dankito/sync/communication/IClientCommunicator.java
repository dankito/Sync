package net.dankito.sync.communication;

import net.dankito.sync.communication.callbacks.SendRequestCallback;
import net.dankito.sync.communication.message.DeviceInfo;
import net.dankito.sync.devices.DiscoveredDevice;


public interface IClientCommunicator {

  void getDeviceInfo(DiscoveredDevice remoteDevice, SendRequestCallback<DeviceInfo> callback);

}