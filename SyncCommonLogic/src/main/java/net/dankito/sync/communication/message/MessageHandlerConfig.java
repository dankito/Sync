package net.dankito.sync.communication.message;

import net.dankito.sync.communication.callback.IsSynchronizationPermittedHandler;
import net.dankito.sync.devices.INetworkSettings;

import javax.inject.Named;


@Named
public class MessageHandlerConfig {

  protected INetworkSettings networkSettings;

  protected ChallengeHandler challengeHandler;

  protected IsSynchronizationPermittedHandler isSynchronizationPermittedHandler;


  public MessageHandlerConfig(INetworkSettings networkSettings, ChallengeHandler challengeHandler, IsSynchronizationPermittedHandler permissionHandler) {
    this.networkSettings = networkSettings;
    this.challengeHandler = challengeHandler;
    this.isSynchronizationPermittedHandler = permissionHandler;
  }


  public INetworkSettings getNetworkSettings() {
    return networkSettings;
  }

  public ChallengeHandler getChallengeHandler() {
    return challengeHandler;
  }

  public IsSynchronizationPermittedHandler getIsSynchronizationPermittedHandler() {
    return isSynchronizationPermittedHandler;
  }

}
