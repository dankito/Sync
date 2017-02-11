package net.dankito.sync.communication;

import net.dankito.communication.IMessageHandler;
import net.dankito.communication.IMessageSerializer;
import net.dankito.communication.IRequestReceiver;
import net.dankito.communication.IRequestSender;
import net.dankito.communication.JsonMessageSerializer;
import net.dankito.communication.RequestReceiver;
import net.dankito.communication.RequestReceiverCallback;
import net.dankito.communication.RequestSender;
import net.dankito.communication.SocketHandler;
import net.dankito.communication.message.Request;
import net.dankito.communication.message.Response;
import net.dankito.communication.message.ResponseErrorType;
import net.dankito.sync.communication.callback.ClientCommunicatorListener;
import net.dankito.sync.communication.callback.SendRequestCallback;
import net.dankito.sync.communication.message.DeviceInfo;
import net.dankito.sync.communication.message.MessageHandler;
import net.dankito.sync.communication.message.MessageHandlerConfig;
import net.dankito.sync.communication.message.RequestStartSynchronizationRequestBody;
import net.dankito.sync.communication.message.RequestStartSynchronizationResponseBody;
import net.dankito.sync.devices.DiscoveredDevice;
import net.dankito.sync.devices.INetworkSettings;
import net.dankito.utils.IThreadPool;

import java.net.InetSocketAddress;
import java.net.SocketAddress;

import javax.inject.Named;


@Named
public class TcpSocketClientCommunicator implements IClientCommunicator {

  protected IRequestSender requestSender;

  protected IRequestReceiver requestReceiver;

  protected INetworkSettings networkSettings;


  public TcpSocketClientCommunicator(INetworkSettings networkSettings, IThreadPool threadPool) {
    setupDependencies(networkSettings, threadPool);
  }

  protected void setupDependencies(INetworkSettings networkSettings, IThreadPool threadPool) {
    this.networkSettings = networkSettings;

    MessageHandlerConfig messageHandlerConfig = new MessageHandlerConfig(networkSettings);

    SocketHandler socketHandler = new SocketHandler();
    IMessageHandler messageHandler = new MessageHandler(messageHandlerConfig);
    IMessageSerializer messageSerializer = new JsonMessageSerializer(messageHandler);

    this.requestSender = new RequestSender(socketHandler, messageSerializer, threadPool);

    this.requestReceiver = new RequestReceiver(socketHandler, messageHandler, messageSerializer, threadPool);
  }


  @Override
  public void start(final int desiredCommunicatorPort, final ClientCommunicatorListener listener) {
    requestReceiver.start(desiredCommunicatorPort, new RequestReceiverCallback() {
      @Override
      public void started(IRequestReceiver requestReceiver, boolean couldStartReceiver, int messagesReceiverPort, Exception startException) {
        listener.started(couldStartReceiver, messagesReceiverPort, startException);
      }
    });
  }

  @Override
  public void stop() {
    requestReceiver.close();
  }


  @Override
  public void getDeviceInfo(SocketAddress destinationAddress, final SendRequestCallback<DeviceInfo> callback) {
    requestSender.sendRequestAndReceiveResponseAsync(destinationAddress,
        new Request(CommunicatorConfig.GET_DEVICE_INFO_METHOD_NAME), new net.dankito.communication.callback.SendRequestCallback<DeviceInfo>() {
          @Override
          public void done(Response<DeviceInfo> response) {
            callback.done(mapResponse(response));
          }
        });
  }


  @Override
  public void requestStartSynchronization(DiscoveredDevice remoteDevice, final SendRequestCallback<RequestStartSynchronizationResponseBody> callback) {
    Request<RequestStartSynchronizationRequestBody> request = new Request<>(CommunicatorConfig.REQUEST_START_SYNCHRONIZATION_METHOD_NAME,
        new RequestStartSynchronizationRequestBody(networkSettings.getLocalHostDevice().getUniqueDeviceId()));

    requestSender.sendRequestAndReceiveResponseAsync(getSocketAddressFromDevice(remoteDevice), request,
        new net.dankito.communication.callback.SendRequestCallback<RequestStartSynchronizationResponseBody>() {
          @Override
          public void done(Response<RequestStartSynchronizationResponseBody> response) {
            callback.done(mapResponse(response));
          }
        });
  }


  protected SocketAddress getSocketAddressFromDevice(DiscoveredDevice device) {
    return new InetSocketAddress(device.getAddress(), device.getMessagesPort());
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
