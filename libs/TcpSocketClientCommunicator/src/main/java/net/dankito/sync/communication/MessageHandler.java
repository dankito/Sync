package net.dankito.sync.communication;

import net.dankito.sync.communication.message.DeviceInfo;
import net.dankito.sync.communication.message.Request;
import net.dankito.sync.communication.message.Response;
import net.dankito.sync.devices.INetworkSettings;


public class MessageHandler implements IMessageHandler {

  protected INetworkSettings networkConfigurationManager;


  public MessageHandler(INetworkSettings networkConfigurationManager) {
    this.networkConfigurationManager = networkConfigurationManager;
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
    return new Response(DeviceInfo.fromDevice(networkConfigurationManager.getLocalHostDevice()));
  }

}
