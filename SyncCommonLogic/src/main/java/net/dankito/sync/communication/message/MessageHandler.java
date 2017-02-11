package net.dankito.sync.communication.message;

import net.dankito.communication.IMessageHandler;
import net.dankito.communication.callback.RequestHandlerCallback;
import net.dankito.communication.message.Request;
import net.dankito.communication.message.Response;
import net.dankito.sync.Device;
import net.dankito.sync.communication.CommunicatorConfig;
import net.dankito.sync.devices.INetworkSettings;


public class MessageHandler implements IMessageHandler {

  protected MessageHandlerConfig config;

  protected INetworkSettings networkSettings;


  public MessageHandler(MessageHandlerConfig config) {
    this.config = config;

    this.networkSettings = config.getNetworkSettings();
  }


  @Override
  public void handleReceivedRequest(Request request, RequestHandlerCallback callback) {
    switch(request.getMethod()) {
      case CommunicatorConfig.GET_DEVICE_INFO_METHOD_NAME:
        callback.done(handleGetDeviceInfoRequest(request));
      case CommunicatorConfig.REQUEST_START_SYNCHRONIZATION_METHOD_NAME:
        handleRequestStartSynchronizationRequest(request, callback);
    }
  }


  protected Response handleGetDeviceInfoRequest(Request request) {
    return new Response(DeviceInfo.fromDevice(networkSettings.getLocalHostDevice()));
  }

  protected void handleRequestStartSynchronizationRequest(Request<RequestStartSynchronizationRequestBody> request, final RequestHandlerCallback callback) {
    RequestStartSynchronizationRequestBody body = request.getBody();
    String remoteDeviceUniqueId = body.getUniqueDeviceId();

    if(isSynchronizingPermitted(remoteDeviceUniqueId) == false) {
      callback.done(new Response<RequestStartSynchronizationResponseBody>(new RequestStartSynchronizationResponseBody(RequestStartSynchronizationResult.DENIED)));
    }
    else {
      callback.done(new Response<RequestStartSynchronizationResponseBody>(new RequestStartSynchronizationResponseBody(RequestStartSynchronizationResult.ALLOWED,
          networkSettings.getSynchronizationPort())));
    }
  }

  protected boolean isSynchronizingPermitted(String remoteDeviceUniqueId) {
    for(Device synchronizedDevice : networkSettings.getLocalConfig().getSynchronizedDevices()) {
      if(synchronizedDevice.getUniqueDeviceId().equals(remoteDeviceUniqueId)) {
        return true;
      }
    }

    return false;
  }


  @Override
  public Class getRequestBodyClassForMethod(String methodName) throws Exception {
    switch(methodName) {
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
      case CommunicatorConfig.REQUEST_START_SYNCHRONIZATION_METHOD_NAME:
        return RequestStartSynchronizationResponseBody.class;
      default:
        throw new Exception("Don't know how to deserialize response of method " + methodName); // TODO: translate
    }
  }

}
