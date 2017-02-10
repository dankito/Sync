package net.dankito.communication;

import net.dankito.communication.callback.SendRequestCallback;
import net.dankito.communication.message.Request;

import java.net.SocketAddress;


public interface IRequestSender {

  void sendRequestAndReceiveResponseAsync(SocketAddress destinationAddress, Request request, SendRequestCallback callback);

}
