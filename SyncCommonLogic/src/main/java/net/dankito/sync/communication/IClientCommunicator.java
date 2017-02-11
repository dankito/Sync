package net.dankito.sync.communication;

import net.dankito.sync.communication.callback.ClientCommunicatorListener;
import net.dankito.sync.communication.callback.SendRequestCallback;
import net.dankito.sync.communication.message.DeviceInfo;
import net.dankito.sync.communication.message.RequestStartSynchronizationResponseBody;
import net.dankito.sync.devices.DiscoveredDevice;

import java.net.SocketAddress;


public interface IClientCommunicator {

  void start(int desiredCommunicatorPort, ClientCommunicatorListener listener);

  void stop();

  void getDeviceInfo(SocketAddress destinationAddress, SendRequestCallback<DeviceInfo> callback);

  void requestStartSynchronization(DiscoveredDevice remoteDevice, SendRequestCallback<RequestStartSynchronizationResponseBody> callback);

}
