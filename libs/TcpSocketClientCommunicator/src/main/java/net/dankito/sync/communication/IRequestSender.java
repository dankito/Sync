package net.dankito.sync.communication;

import net.dankito.sync.communication.callback.SendRequestCallback;
import net.dankito.sync.communication.message.Request;

import java.net.SocketAddress;


public interface IRequestSender {

  void sendRequestAndReceiveResponseAsync(SocketAddress destinationAddress, Request request, SendRequestCallback callback);

}
