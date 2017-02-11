package net.dankito.sync.communication.message;

import net.dankito.sync.communication.IRequestHandler;
import net.dankito.sync.devices.INetworkSettings;
import net.dankito.sync.synchronization.ISyncManager;

import javax.inject.Inject;
import javax.inject.Named;


@Named
public class MessageHandlerConfig {

  protected INetworkSettings networkSettings;

  protected IRequestHandler requestStartSynchronizationHandler;


  public MessageHandlerConfig(INetworkSettings networkSettings, IRequestHandler requestStartSynchronizationHandler) {
    this.networkSettings = networkSettings;
    this.requestStartSynchronizationHandler = requestStartSynchronizationHandler;
  }

  @Inject
  public MessageHandlerConfig(INetworkSettings networkSettings, ISyncManager syncManager) {
    this(networkSettings, syncManager.getRequestStartSynchronizationHandler());
  }


  public INetworkSettings getNetworkSettings() {
    return networkSettings;
  }

  public IRequestHandler getRequestStartSynchronizationHandler() {
    return requestStartSynchronizationHandler;
  }

}
