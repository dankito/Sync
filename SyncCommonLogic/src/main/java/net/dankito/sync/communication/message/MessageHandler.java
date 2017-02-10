package net.dankito.sync.communication.message;

import net.dankito.sync.Device;
import net.dankito.sync.communication.CommunicationConfig;
import net.dankito.sync.communication.IMessageHandler;


public class MessageHandler implements IMessageHandler {

  protected Device localDevice;


  public MessageHandler(Device localDevice) {
    this.localDevice = localDevice;
  }


  @Override
  public Response handleReceivedRequest(Request request) {
    switch(request.getMethod()) {
      case CommunicationConfig.GET_DEVICE_INFO_METHOD_NAME:
        return handleGetDeviceInfoRequest(request);
    }

    return null;
  }


  @Override
  public Class getResponseBodyClassForMethod(String methodName) throws Exception {
    switch(methodName) {
      case CommunicationConfig.GET_DEVICE_INFO_METHOD_NAME:
        return DeviceInfo.class;
      default:
        throw new Exception("Don't know how to deserialize response of method " + methodName); // TODO: translate
    }
  }

  @Override
  public Class getRequestBodyClassForMethod(String methodName) throws Exception {
    switch(methodName) {
      case CommunicationConfig.GET_DEVICE_INFO_METHOD_NAME:
        return null; // requests without request bodies
      default:
        throw new Exception("Don't know how to deserialize response of method " + methodName); // TODO: translate
    }
  }


  protected Response handleGetDeviceInfoRequest(Request request) {
    return new Response(DeviceInfo.fromDevice(localDevice));
  }

}
