package net.dankito.sync.communication.message;

import net.dankito.communication.IMessageHandler;
import net.dankito.communication.callback.RequestHandlerCallback;
import net.dankito.communication.message.Request;
import net.dankito.communication.message.Response;
import net.dankito.sync.communication.CommunicatorConfig;


public class MessageHandler implements IMessageHandler {

  protected MessageHandlerConfig config;


  public MessageHandler(MessageHandlerConfig config) {
    this.config = config;
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
    return new Response(DeviceInfo.fromDevice(config.getNetworkSettings().getLocalHostDevice()));
  }

  protected void handleRequestStartSynchronizationRequest(Request request, final RequestHandlerCallback callback) {
    config.getRequestStartSynchronizationHandler().handle(request, new net.dankito.sync.communication.callback.RequestHandlerCallback() {
      @Override
      public void done(Response response) {
        callback.done(response);
      }
    });
  }


  @Override
  public Class getRequestBodyClassForMethod(String methodName) throws Exception {
    switch(methodName) {
      case CommunicatorConfig.GET_DEVICE_INFO_METHOD_NAME:
      case CommunicatorConfig.REQUEST_START_SYNCHRONIZATION_METHOD_NAME:
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
