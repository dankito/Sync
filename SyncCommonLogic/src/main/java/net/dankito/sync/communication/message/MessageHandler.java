package net.dankito.sync.communication.message;

import net.dankito.communication.IMessageHandler;
import net.dankito.communication.callback.RequestHandlerCallback;
import net.dankito.communication.message.Request;
import net.dankito.communication.message.Response;
import net.dankito.sync.Device;
import net.dankito.sync.communication.CommunicatorConfig;
import net.dankito.sync.communication.callback.IsSynchronizationPermittedHandler;
import net.dankito.sync.communication.callback.ShouldPermitSynchronizingWithDeviceCallback;
import net.dankito.sync.devices.DiscoveredDevice;
import net.dankito.sync.devices.INetworkSettings;

import java.util.List;


public class MessageHandler implements IMessageHandler {

  protected MessageHandlerConfig config;

  protected INetworkSettings networkSettings;

  protected ChallengeHandler challengeHandler;


  public MessageHandler(MessageHandlerConfig config) {
    this.config = config;

    this.networkSettings = config.getNetworkSettings();

    this.challengeHandler = config.getChallengeHandler();
  }


  @Override
  public void handleReceivedRequest(Request request, RequestHandlerCallback callback) {
    switch(request.getMethod()) {
      case CommunicatorConfig.GET_DEVICE_INFO_METHOD_NAME:
        callback.done(handleGetDeviceInfoRequest(request));
        break;
      case CommunicatorConfig.REQUEST_PERMIT_SYNCHRONIZATION_METHOD_NAME:
        handleRequestPermitSynchronizationRequest(request, callback);
        break;
      case CommunicatorConfig.RESPONSE_TO_SYNCHRONIZATION_PERMITTING_CHALLENGE_METHOD_NAME:
        handleRespondToSynchronizationPermittingChallengeRequest(request, callback);
        break;
      case CommunicatorConfig.REQUEST_START_SYNCHRONIZATION_METHOD_NAME:
        handleRequestStartSynchronizationRequest(request, callback);
        break;
    }
  }


  protected Response handleGetDeviceInfoRequest(Request request) {
    return new Response(DeviceInfo.fromDevice(networkSettings.getLocalHostDevice()));
  }


  protected void handleRequestPermitSynchronizationRequest(Request<DeviceInfo> request, final RequestHandlerCallback callback) {
    final DeviceInfo remoteDeviceInfo = request.getBody();
    final IsSynchronizationPermittedHandler permittingHandler = config.getIsSynchronizationPermittedHandler();

    permittingHandler.shouldPermitSynchronizingWithDevice(remoteDeviceInfo, new ShouldPermitSynchronizingWithDeviceCallback() {
      @Override
      public void done(DeviceInfo remoteDeviceInfo, boolean permitsSynchronization) {
        handleShouldPermitSynchronizingWithDeviceResult(remoteDeviceInfo, permitsSynchronization, permittingHandler, callback);
      }
    });
  }

  protected void handleShouldPermitSynchronizingWithDeviceResult(DeviceInfo remoteDeviceInfo, boolean permitsSynchronization, IsSynchronizationPermittedHandler permittingHandler, RequestHandlerCallback callback) {
    if(permitsSynchronization) {
      NonceToResponsePair nonceToResponsePair = challengeHandler.createChallengeForDevice(remoteDeviceInfo);
      permittingHandler.showCorrectResponseToUserNonBlocking(remoteDeviceInfo, nonceToResponsePair.getCorrectResponse());

      callback.done(new Response<RequestPermitSynchronizationResponseBody>(new RequestPermitSynchronizationResponseBody(
          RequestPermitSynchronizationResult.RESPOND_TO_CHALLENGE, nonceToResponsePair.getNonce())));
    }
    else {
      callback.done(new Response<RequestPermitSynchronizationResponseBody>(new RequestPermitSynchronizationResponseBody(RequestPermitSynchronizationResult.DENIED)));
    }
  }

  protected void handleRespondToSynchronizationPermittingChallengeRequest(Request<RespondToSynchronizationPermittingChallengeRequestBody> request, RequestHandlerCallback callback) {
    String nonce = request.getBody().getNonce();
    RespondToSynchronizationPermittingChallengeResponseBody responseBody = null;

    if(challengeHandler.isResponseOk(nonce, request.getBody().getChallengeResponse())) {
      addToPermittedSynchronizedDevices(request.getBody());

      responseBody = new RespondToSynchronizationPermittingChallengeResponseBody(networkSettings.getSynchronizationPort());
    }
    else {
      responseBody = createWrongCodeResponse(nonce);
    }

    callback.done(new Response<RespondToSynchronizationPermittingChallengeResponseBody>(responseBody));
  }

  protected void addToPermittedSynchronizedDevices(RespondToSynchronizationPermittingChallengeRequestBody requestBody) {
    DeviceInfo deviceInfo = challengeHandler.getDeviceInfoForNonce(requestBody.getNonce());
    String deviceUniqueId = deviceInfo.getUniqueDeviceId();

    DiscoveredDevice discoveredDevice = networkSettings.getDiscoveredDevice(deviceUniqueId);
    if(discoveredDevice != null) {
      discoveredDevice.setSynchronizationPort(requestBody.getSynchronizationPort());

      networkSettings.addConnectedDevicePermittedToSynchronize(discoveredDevice);
    }
  }

  protected RespondToSynchronizationPermittingChallengeResponseBody createWrongCodeResponse(String nonce) {
    RespondToSynchronizationPermittingChallengeResponseBody responseBody;
    int countRetriesLeft = challengeHandler.getCountRetriesLeftForNonce(nonce);
    if(countRetriesLeft > 0) {
      responseBody = new RespondToSynchronizationPermittingChallengeResponseBody(RespondToSynchronizationPermittingChallengeResult.WRONG_CODE, countRetriesLeft);
    }
    else {
      responseBody = new RespondToSynchronizationPermittingChallengeResponseBody(RespondToSynchronizationPermittingChallengeResult.DENIED);
    }
    return responseBody;
  }


  protected void handleRequestStartSynchronizationRequest(Request<RequestStartSynchronizationRequestBody> request, final RequestHandlerCallback callback) {
    RequestStartSynchronizationRequestBody body = request.getBody();

    if(isDevicePermittedToSynchronize(body.getUniqueDeviceId()) == false) {
      callback.done(new Response<RequestStartSynchronizationResponseBody>(new RequestStartSynchronizationResponseBody(RequestStartSynchronizationResult.DENIED)));
    }
    else {
      DiscoveredDevice permittedSynchronizedDevice = networkSettings.getDiscoveredDevice(body.getUniqueDeviceId());
      if(permittedSynchronizedDevice != null) {
        permittedSynchronizedDevice.setSynchronizationPort(body.getSynchronizationPort());

        networkSettings.addConnectedDevicePermittedToSynchronize(permittedSynchronizedDevice);
      }

      callback.done(new Response<RequestStartSynchronizationResponseBody>(new RequestStartSynchronizationResponseBody(RequestStartSynchronizationResult.ALLOWED,
          networkSettings.getSynchronizationPort())));
    }
  }

  protected boolean isDevicePermittedToSynchronize(String remoteDeviceUniqueId) {
    List<Device> synchronizedDevices = networkSettings.getLocalConfig().getSynchronizedDevices();

    for(Device synchronizedDevice : synchronizedDevices) {
      if(synchronizedDevice.getUniqueDeviceId().equals(remoteDeviceUniqueId)) {
        return true;
      }
    }

    return false;
  }


  @Override
  public Class getRequestBodyClassForMethod(String methodName) throws Exception {
    switch(methodName) {
      case CommunicatorConfig.REQUEST_PERMIT_SYNCHRONIZATION_METHOD_NAME:
        return DeviceInfo.class;
      case CommunicatorConfig.RESPONSE_TO_SYNCHRONIZATION_PERMITTING_CHALLENGE_METHOD_NAME:
        return RespondToSynchronizationPermittingChallengeRequestBody.class;
      case CommunicatorConfig.REQUEST_START_SYNCHRONIZATION_METHOD_NAME:
        return RequestStartSynchronizationRequestBody.class;
      case CommunicatorConfig.GET_DEVICE_INFO_METHOD_NAME:
        return null; // requests without request bodies
      default:
        throw new Exception("Don't know how to deserialize response of method " + methodName); // TODO: translate
    }
  }

  @Override
  public Class getResponseBodyClassForMethod(String methodName) throws Exception {
    switch(methodName) {
      case CommunicatorConfig.GET_DEVICE_INFO_METHOD_NAME:
        return DeviceInfo.class;
      case CommunicatorConfig.REQUEST_PERMIT_SYNCHRONIZATION_METHOD_NAME:
        return RequestPermitSynchronizationResponseBody.class;
      case CommunicatorConfig.RESPONSE_TO_SYNCHRONIZATION_PERMITTING_CHALLENGE_METHOD_NAME:
        return RespondToSynchronizationPermittingChallengeResponseBody.class;
      case CommunicatorConfig.REQUEST_START_SYNCHRONIZATION_METHOD_NAME:
        return RequestStartSynchronizationResponseBody.class;
      default:
        throw new Exception("Don't know how to deserialize response of method " + methodName); // TODO: translate
    }
  }

}
