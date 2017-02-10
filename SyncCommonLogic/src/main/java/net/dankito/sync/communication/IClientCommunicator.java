package net.dankito.sync.communication;

import net.dankito.sync.communication.callback.ClientCommunicatorListener;
import net.dankito.sync.communication.callback.SendRequestCallback;
import net.dankito.sync.communication.message.DeviceInfo;
import net.dankito.sync.devices.DiscoveredDevice;


public interface IClientCommunicator {

  void start(int desiredCommunicatorPort, ClientCommunicatorListener listener);

  void getDeviceInfo(DiscoveredDevice remoteDevice, SendRequestCallback<DeviceInfo> callback);

}
