package net.dankito.sync.communication;

import net.dankito.sync.communication.callback.SendRequestCallback;
import net.dankito.sync.communication.message.DeviceInfo;
import net.dankito.sync.communication.message.MessageHandler;
import net.dankito.sync.communication.message.Request;
import net.dankito.sync.devices.DiscoveredDevice;
import net.dankito.utils.IThreadPool;

import java.net.InetSocketAddress;


public class TcpSocketClientCommunicator implements IClientCommunicator {

  protected IRequestSender requestSender;


  public TcpSocketClientCommunicator(IRequestSender requestSender) {
    this.requestSender = requestSender;
  }


  @Override
  public void getDeviceInfo(DiscoveredDevice remoteDevice, final SendRequestCallback<DeviceInfo> callback) {
    requestSender.sendRequestAndReceiveResponseAsync(new InetSocketAddress(remoteDevice.getAddress(), remoteDevice.getMessagesPort()),
        new Request(CommunicationConfig.GET_DEVICE_INFO_METHOD_NAME), callback);
  }

}
