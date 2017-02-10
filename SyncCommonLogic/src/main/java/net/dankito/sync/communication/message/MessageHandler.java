package net.dankito.sync.communication.message;

import net.dankito.communication.IMessageHandler;
import net.dankito.communication.message.Request;
import net.dankito.communication.message.Response;
import net.dankito.sync.Device;
import net.dankito.sync.communication.CommunicatorConfig;


public class MessageHandler implements IMessageHandler {

  protected Device localDevice;


  public MessageHandler(Device localDevice) {
    this.localDevice = localDevice;
  }


  @Override
  public Response handleReceivedRequest(Request request) {
    switch(request.getMethod()) {
      case CommunicatorConfig.GET_DEVICE_INFO_METHOD_NAME:
        return handleGetDeviceInfoRequest(request);
    }

    return null;
  }


  @Override
  public Class getResponseBodyClassForMethod(String methodName) throws Exception {
    switch(methodName) {
      case CommunicatorConfig.GET_DEVICE_INFO_METHOD_NAME:
        return DeviceInfo.class;
      default:
        throw new Exception("Don't know how to deserialize response of method " + methodName); // TODO: translate
    }
  }

  @Override
  public Class getRequestBodyClassForMethod(String methodName) throws Exception {
    switch(methodName) {
      case CommunicatorConfig.GET_DEVICE_INFO_METHOD_NAME:
        return null; // requests without request bodies
      default:
        throw new Exception("Don't know how to deserialize response of method " + methodName); // TODO: translate
    }
  }


  protected Response handleGetDeviceInfoRequest(Request request) {
    return new Response(DeviceInfo.fromDevice(localDevice));
  }

}
