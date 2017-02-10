package net.dankito.sync.communication;

import net.dankito.sync.communication.callback.ClientCommunicatorListener;
import net.dankito.sync.devices.IDevicesManager;
import net.dankito.sync.devices.INetworkSettings;
import net.dankito.utils.IThreadPool;

import javax.inject.Named;


@Named
public class CommunicationManager implements ICommunicationManager {

  protected IClientCommunicator clientCommunicator;

  protected IDevicesManager devicesManager;

  protected INetworkSettings networkSettings;

  protected IThreadPool threadPool;


  public CommunicationManager(IDevicesManager devicesManager, INetworkSettings networkSettings, IThreadPool threadPool) {
    this.devicesManager = devicesManager;
    this.networkSettings = networkSettings;
    this.threadPool = threadPool;

    this.clientCommunicator = new TcpSocketClientCommunicator(networkSettings.getLocalHostDevice(), threadPool);
  }


  @Override
  public void startAsync() {
    clientCommunicator.start(CommunicatorConfig.DEFAULT_MESSAGES_RECEIVER_PORT, new ClientCommunicatorListener() {
      @Override
      public void started(boolean couldStartMessagesReceiver, int messagesReceiverPort, Exception startException) {
        if(couldStartMessagesReceiver) {
          successfullyStartedClientCommunicator(messagesReceiverPort);
        }
        else {
          startingClientCommunicatorFailed(startException);
        }
      }
    });
  }

  @Override
  public void stop() {
    devicesManager.stop();

    clientCommunicator.stop();
  }


  protected void startingClientCommunicatorFailed(Exception startException) {
    // TODO: what to do in error case?
  }

  protected void successfullyStartedClientCommunicator(int messagesReceiverPort) {
    networkSettings.setMessagePort(messagesReceiverPort);

    devicesManager.start();
  }
}
