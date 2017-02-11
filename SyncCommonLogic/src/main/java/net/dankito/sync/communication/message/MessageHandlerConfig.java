package net.dankito.sync.communication.message;

import net.dankito.sync.communication.callback.IsSynchronizationPermittedHandler;
import net.dankito.sync.devices.INetworkSettings;

import javax.inject.Named;


@Named
public class MessageHandlerConfig {

  protected INetworkSettings networkSettings;

  protected IsSynchronizationPermittedHandler isSynchronizationPermittedHandler;


  public MessageHandlerConfig(INetworkSettings networkSettings, IsSynchronizationPermittedHandler permissionHandler) {
    this.networkSettings = networkSettings;
    this.isSynchronizationPermittedHandler = permissionHandler;
  }


  public INetworkSettings getNetworkSettings() {
    return networkSettings;
  }

  public IsSynchronizationPermittedHandler getIsSynchronizationPermittedHandler() {
    return isSynchronizationPermittedHandler;
  }

}
