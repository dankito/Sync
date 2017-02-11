package net.dankito.sync.communication.message;

import net.dankito.sync.devices.INetworkSettings;

import javax.inject.Named;


@Named
public class MessageHandlerConfig {

  protected INetworkSettings networkSettings;


  public MessageHandlerConfig(INetworkSettings networkSettings) {
    this.networkSettings = networkSettings;
  }


  public INetworkSettings getNetworkSettings() {
    return networkSettings;
  }

}
