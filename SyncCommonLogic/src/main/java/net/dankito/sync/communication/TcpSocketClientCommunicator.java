package net.dankito.sync.communication;

import net.dankito.communication.IRequestSender;
import net.dankito.communication.message.Request;
import net.dankito.communication.message.Response;
import net.dankito.communication.message.ResponseErrorType;
import net.dankito.sync.communication.callback.SendRequestCallback;
import net.dankito.sync.communication.message.DeviceInfo;
import net.dankito.sync.devices.DiscoveredDevice;

import java.net.InetSocketAddress;


public class TcpSocketClientCommunicator implements IClientCommunicator {

  protected IRequestSender requestSender;


  public TcpSocketClientCommunicator(IRequestSender requestSender) {
    this.requestSender = requestSender;
  }


  @Override
  public void getDeviceInfo(DiscoveredDevice remoteDevice, final SendRequestCallback<DeviceInfo> callback) {
    requestSender.sendRequestAndReceiveResponseAsync(new InetSocketAddress(remoteDevice.getAddress(), remoteDevice.getMessagesPort()),
        new Request(CommunicatorConfig.GET_DEVICE_INFO_METHOD_NAME), new net.dankito.communication.callback.SendRequestCallback<DeviceInfo>() {
          @Override
          public void done(Response<DeviceInfo> response) {
            callback.done(mapResponse(response));
          }
        });
  }


  protected net.dankito.sync.communication.message.Response mapResponse(Response response) {
    return new net.dankito.sync.communication.message.Response(response.isCouldHandleMessage(), mapResponseErrorType(response.getErrorType()),
        response.getError(), response.getBody());
  }

  protected net.dankito.sync.communication.message.ResponseErrorType mapResponseErrorType(ResponseErrorType errorType) {
    if(errorType != null) {
      return net.dankito.sync.communication.message.ResponseErrorType.valueOf(errorType.name());
    }

    return null;
  }

}
