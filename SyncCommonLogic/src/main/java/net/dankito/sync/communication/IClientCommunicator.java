package net.dankito.sync.communication;

import net.dankito.sync.communication.callback.ClientCommunicatorListener;
import net.dankito.sync.communication.callback.SendRequestCallback;
import net.dankito.sync.communication.message.DeviceInfo;

import java.net.SocketAddress;


public interface IClientCommunicator {

  void start(int desiredCommunicatorPort, ClientCommunicatorListener listener);

  void stop();

  void getDeviceInfo(SocketAddress destinationAddress, SendRequestCallback<DeviceInfo> callback);

}
