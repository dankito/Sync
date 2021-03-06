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
import net.dankito.sync.communication.callback.IsSynchronizationPermittedHandler;
import net.dankito.sync.communication.callback.SendRequestCallback;
import net.dankito.sync.communication.message.ChallengeHandler;
import net.dankito.sync.communication.message.DeviceInfo;
import net.dankito.sync.communication.message.MessageHandler;
import net.dankito.sync.communication.message.MessageHandlerConfig;
import net.dankito.sync.communication.message.RequestPermitSynchronizationResponseBody;
import net.dankito.sync.communication.message.RequestPermitSynchronizationResult;
import net.dankito.sync.communication.message.RequestStartSynchronizationRequestBody;
import net.dankito.sync.communication.message.RequestStartSynchronizationResponseBody;
import net.dankito.sync.communication.message.RespondToSynchronizationPermittingChallengeRequestBody;
import net.dankito.sync.communication.message.RespondToSynchronizationPermittingChallengeResponseBody;
import net.dankito.sync.communication.message.RespondToSynchronizationPermittingChallengeResult;
import net.dankito.sync.devices.DiscoveredDevice;
import net.dankito.sync.devices.INetworkSettings;
import net.dankito.utils.IThreadPool;
import net.dankito.utils.services.IBase64Service;

import java.net.InetSocketAddress;
import java.net.SocketAddress;

import javax.inject.Named;


@Named
public class TcpSocketClientCommunicator implements IClientCommunicator {

  protected IRequestSender requestSender;

  protected IRequestReceiver requestReceiver;

  protected INetworkSettings networkSettings;

  protected ChallengeHandler challengeHandler;


  public TcpSocketClientCommunicator(INetworkSettings networkSettings, IsSynchronizationPermittedHandler permissionHandler, IBase64Service base64Service, IThreadPool threadPool) {
    setupDependencies(networkSettings, permissionHandler, base64Service, threadPool);
  }

  protected void setupDependencies(INetworkSettings networkSettings, IsSynchronizationPermittedHandler permissionHandler, IBase64Service base64Service, IThreadPool threadPool) {
    this.networkSettings = networkSettings;
    this.challengeHandler = new ChallengeHandler(base64Service);

    MessageHandlerConfig messageHandlerConfig = new MessageHandlerConfig(networkSettings, challengeHandler, permissionHandler);

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
  public void requestPermitSynchronization(final DiscoveredDevice remoteDevice, final SendRequestCallback<RequestPermitSynchronizationResponseBody> callback) {
    networkSettings.addDevicesAskedForPermittingSynchronization(remoteDevice);

    Request<DeviceInfo> request = new Request<>(CommunicatorConfig.REQUEST_PERMIT_SYNCHRONIZATION_METHOD_NAME,
        DeviceInfo.fromDevice(networkSettings.getLocalHostDevice()));

    requestSender.sendRequestAndReceiveResponseAsync(getSocketAddressFromDevice(remoteDevice), request,
        new net.dankito.communication.callback.SendRequestCallback<RequestPermitSynchronizationResponseBody>() {
          @Override
          public void done(Response<RequestPermitSynchronizationResponseBody> response) {
            if(response.isCouldHandleMessage() == false || response.getBody().getResult() != RequestPermitSynchronizationResult.RESPOND_TO_CHALLENGE) {
              networkSettings.removeDevicesAskedForPermittingSynchronization(remoteDevice);
            }

            callback.done(mapResponse(response));
          }
        });
  }

  @Override
  public void respondToSynchronizationPermittingChallenge(final DiscoveredDevice remoteDevice, String nonce, String enteredCode,
                                                          final SendRequestCallback<RespondToSynchronizationPermittingChallengeResponseBody> callback) {
    String challengeResponse = challengeHandler.createChallengeResponse(nonce, enteredCode);

    Request<RespondToSynchronizationPermittingChallengeRequestBody> request = new Request<>(CommunicatorConfig.RESPONSE_TO_SYNCHRONIZATION_PERMITTING_CHALLENGE_METHOD_NAME,
        new RespondToSynchronizationPermittingChallengeRequestBody(nonce, challengeResponse, networkSettings.getSynchronizationPort()));

    requestSender.sendRequestAndReceiveResponseAsync(getSocketAddressFromDevice(remoteDevice), request,
        new net.dankito.communication.callback.SendRequestCallback<RespondToSynchronizationPermittingChallengeResponseBody>() {
          @Override
          public void done(Response<RespondToSynchronizationPermittingChallengeResponseBody> response) {
            handleRespondToSynchronizationPermittingChallengeResponse(remoteDevice, response);

            callback.done(mapResponse(response));
          }
        });
  }

  protected void handleRespondToSynchronizationPermittingChallengeResponse(DiscoveredDevice remoteDevice, Response<RespondToSynchronizationPermittingChallengeResponseBody> response) {
    if(response.isCouldHandleMessage()) {
      RespondToSynchronizationPermittingChallengeResult result = response.getBody().getResult();

      if(response.getBody().getResult() != RespondToSynchronizationPermittingChallengeResult.WRONG_CODE) {
        networkSettings.removeDevicesAskedForPermittingSynchronization(remoteDevice);
      }

      if(result == RespondToSynchronizationPermittingChallengeResult.ALLOWED) {
        remoteDevice.setSynchronizationPort(response.getBody().getSynchronizationPort());
      }
    }
  }


  @Override
  public void requestStartSynchronization(final DiscoveredDevice remoteDevice, final SendRequestCallback<RequestStartSynchronizationResponseBody> callback) {
    networkSettings.addDevicesAskedForPermittingSynchronization(remoteDevice);

    Request<RequestStartSynchronizationRequestBody> request = new Request<>(CommunicatorConfig.REQUEST_START_SYNCHRONIZATION_METHOD_NAME,
        new RequestStartSynchronizationRequestBody(networkSettings.getLocalHostDevice().getUniqueDeviceId(), networkSettings.getSynchronizationPort()));

    requestSender.sendRequestAndReceiveResponseAsync(getSocketAddressFromDevice(remoteDevice), request,
        new net.dankito.communication.callback.SendRequestCallback<RequestStartSynchronizationResponseBody>() {
          @Override
          public void done(Response<RequestStartSynchronizationResponseBody> response) {
            networkSettings.removeDevicesAskedForPermittingSynchronization(remoteDevice);

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
