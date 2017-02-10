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
import net.dankito.sync.Device;
import net.dankito.sync.communication.callback.ClientCommunicatorListener;
import net.dankito.sync.communication.callback.SendRequestCallback;
import net.dankito.sync.communication.message.DeviceInfo;
import net.dankito.sync.communication.message.MessageHandler;
import net.dankito.utils.IThreadPool;

import java.net.SocketAddress;

import javax.inject.Named;


@Named
public class TcpSocketClientCommunicator implements IClientCommunicator {

  protected IRequestSender requestSender;

  protected IRequestReceiver requestReceiver;


  public TcpSocketClientCommunicator(Device localDevice, IThreadPool threadPool) {
    setupDependencies(localDevice, threadPool);
  }

  protected void setupDependencies(Device localDevice, IThreadPool threadPool) {
    SocketHandler socketHandler = new SocketHandler();
    IMessageHandler messageHandler = new MessageHandler(localDevice);
    IMessageSerializer messageSerializer = new JsonMessageSerializer(messageHandler);

    requestSender = new RequestSender(socketHandler, messageSerializer, threadPool);

    requestReceiver = new RequestReceiver(socketHandler, messageHandler, messageSerializer, threadPool);
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
