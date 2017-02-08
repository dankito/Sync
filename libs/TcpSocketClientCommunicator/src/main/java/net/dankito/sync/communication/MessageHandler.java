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


  protected Response handleGetDeviceInfoRequest(Request request) {
    return new Response(DeviceInfo.fromDevice(networkConfigurationManager.getLocalHostDevice()));
  }

}
