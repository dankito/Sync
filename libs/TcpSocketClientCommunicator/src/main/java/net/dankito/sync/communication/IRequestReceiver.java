package net.dankito.sync.communication;

import net.dankito.sync.devices.INetworkSettings;


public interface IRequestReceiver {

  void start(int desiredMessagesReceiverPort, INetworkSettings networkSettings);

  void close();

}
