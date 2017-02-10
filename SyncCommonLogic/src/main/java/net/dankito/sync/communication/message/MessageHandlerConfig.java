package net.dankito.sync.communication.message;

import net.dankito.sync.communication.IRequestHandler;
import net.dankito.sync.devices.INetworkSettings;


public class MessageHandlerConfig {

  protected INetworkSettings networkSettings;

  protected IRequestHandler requestStartSynchronizationHandler;


  public MessageHandlerConfig(INetworkSettings networkSettings, IRequestHandler requestStartSynchronizationHandler) {
    this.networkSettings = networkSettings;
    this.requestStartSynchronizationHandler = requestStartSynchronizationHandler;
  }


  public INetworkSettings getNetworkSettings() {
    return networkSettings;
  }

  public IRequestHandler getRequestStartSynchronizationHandler() {
    return requestStartSynchronizationHandler;
  }

}
