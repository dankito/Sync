package net.dankito.communication;


import net.dankito.communication.callback.SendRequestCallback;
import net.dankito.communication.message.Request;
import net.dankito.communication.message.Response;
import net.dankito.communication.message.ResponseErrorType;
import net.dankito.utils.IThreadPool;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.Socket;
import java.net.SocketAddress;

public class RequestSender implements IRequestSender {

  private static final Logger log = LoggerFactory.getLogger(RequestSender.class);


  protected SocketHandler socketHandler;

  protected IMessageSerializer messageSerializer;

  protected IThreadPool threadPool;


  public RequestSender(SocketHandler socketHandler, IMessageSerializer messageSerializer, IThreadPool threadPool) {
    this.socketHandler = socketHandler;
    this.messageSerializer = messageSerializer;
    this.threadPool = threadPool;
  }


  @Override
  public void sendRequestAndReceiveResponseAsync(final SocketAddress destinationAddress, final Request request, final SendRequestCallback callback) {
    threadPool.runAsync(new Runnable() {
      @Override
      public void run() {
        sendRequestAndReceiveResponse(destinationAddress, request, callback);
      }
    });
  }


  protected void sendRequestAndReceiveResponse(SocketAddress destinationAddress, Request request, SendRequestCallback callback) {
    Socket socket = new Socket();

    try{
      socket.setSoTimeout(0); // disables socket read() timeout
      socket.setReuseAddress(true);
      socket.connect(destinationAddress);

      if(sendRequest(socket, request, callback)) {
        receiveResponse(socket, request, callback);
      }
    } catch(Exception e) {
      log.error("Could not send request to " + destinationAddress + " for request " + request, e);
      callback.done(new Response(ResponseErrorType.SEND_REQUEST_TO_REMOTE, e));
    }
    finally {
      socketHandler.closeSocket(socket);
    }
  }

  protected boolean sendRequest(Socket socket, Request request, SendRequestCallback callback) {
    try {
      byte[] message = messageSerializer.serializeRequest(request);
      SocketResult result = socketHandler.sendMessage(socket, message);

      if(result.isSuccessful() == false) {
        callback.done(new Response(ResponseErrorType.SEND_REQUEST_TO_REMOTE, result.getError()));
      }

      return result.isSuccessful();
    }
    catch(Exception e) {
      log.error("Could not send request " + request + " to client " + (socket != null ? socket.getInetAddress() : ""), e);
      callback.done(new Response(ResponseErrorType.SERIALIZE_REQUEST, e));
    }

    return false;
  }


  protected void receiveResponse(Socket socket, Request request, SendRequestCallback callback) {
    SocketResult result = socketHandler.receiveMessage(socket);

    if(result.isSuccessful() == false) {
      callback.done(new Response(ResponseErrorType.RETRIEVE_RESPONSE, result.getError()));
    }
    else {
      deserializeReceivedResponse(request.getMethod(), result.getReceivedMessage(), callback);
    }
  }

  protected void deserializeReceivedResponse(String methodName, String responseString, SendRequestCallback callback) {
    Response response = messageSerializer.deserializeResponse(methodName, responseString);
    callback.done(response);
  }

}
