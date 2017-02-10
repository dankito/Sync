package net.dankito.communication;


import net.dankito.communication.message.ResponseErrorType;
import net.dankito.communication.message.Request;
import net.dankito.communication.message.Response;
import net.dankito.utils.IThreadPool;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;


public class RequestReceiver implements IRequestReceiver {

  private static final Logger log = LoggerFactory.getLogger(RequestReceiver.class);


  protected SocketHandler socketHandler;

  protected IThreadPool threadPool;

  protected IMessageHandler messageHandler;

  protected IMessageSerializer messageSerializer;

  protected ServerSocket receiverSocket;

  protected Thread receiverThread;


  public RequestReceiver(SocketHandler socketHandler, IMessageHandler messageHandler, IMessageSerializer messageSerializer, IThreadPool threadPool) {
    this.socketHandler = socketHandler;
    this.messageHandler = messageHandler;
    this.messageSerializer = messageSerializer;
    this.threadPool = threadPool;
  }


  public void close() {
    if(receiverSocket != null) {
      try {
        receiverSocket.close();
      } catch(Exception e) { log.error("Could not close receiver socket", e); }

      receiverSocket = null;
    }

    if(receiverThread != null) {
      try {
        receiverThread.join(100);
      } catch(Exception e) { log.error("Could not stop receiver thread", e); }

      receiverThread = null;
    }
  }


  public void start(int desiredMessagesReceiverPort, RequestReceiverCallback callback) {
    createReceiverSocketAsync(desiredMessagesReceiverPort, callback);
  }

  protected void createReceiverSocketAsync(final int desiredPort, final RequestReceiverCallback callback) {
    receiverThread = new Thread(new Runnable() {
      @Override
      public void run() {
        createReceiverSocket(desiredPort, callback);
      }
    });

    receiverThread.start();
  }

  protected void createReceiverSocket(int desiredPort, RequestReceiverCallback callback) {
    try {
      receiverSocket = new ServerSocket(desiredPort);

      receiverSocketBoundToPort(desiredPort, callback);

      waitForArrivingRequests();
    } catch(Exception e) {
      log.error("Could not bind receiverSocket to port " + desiredPort, e);
      if(e instanceof IOException) {
        createReceiverSocket(desiredPort + 1, callback);
      }
      else {
        creatingReceiverSocketFailed(desiredPort, e, callback);
      }
    }
  }

  protected void creatingReceiverSocketFailed(int port, Exception exception, RequestReceiverCallback callback) {
    callback.started(this, false, port, exception);
  }

  protected void receiverSocketBoundToPort(int port, RequestReceiverCallback callback) {
    callback.started(this, true, port, null);
  }

  protected void waitForArrivingRequests() {
    while(Thread.currentThread().isInterrupted() == false) {
      try {
        Socket clientSocket = receiverSocket.accept();

        receivedRequestAsync(clientSocket);
      } catch(Exception e) {
        if(e instanceof InterruptedException || e instanceof SocketException) {
          break;
        }
        else {
          log.error("An error occurred accepting client request", e);
        }
      }
    }
  }

  protected void receivedRequestAsync(final Socket clientSocket) {
    threadPool.runAsync(new Runnable() {
      @Override
      public void run() {
        receivedRequest(clientSocket);
      }
    });
  }

  protected void receivedRequest(Socket clientSocket) {
    SocketResult result = socketHandler.receiveMessage(clientSocket);

    if(result.isSuccessful()) {
      receivedRequest(clientSocket, result.getReceivedMessage());
    }
    // TODO: what to do in error case?

    socketHandler.closeSocket(clientSocket);
  }

  protected void receivedRequest(Socket clientSocket, String requestString) {
    try {
      Request request = messageSerializer.deserializeRequest(requestString);
      receivedRequest(clientSocket, request);
    } catch(Exception e) {
      log.error("Could not deserialize request string " + requestString, e);
      dispatchResponseToRequest(clientSocket, null, new Response(ResponseErrorType.DESERIALIZE_REQUEST, e));
    }
  }

  protected void receivedRequest(Socket clientSocket, Request request) {
    Response response = messageHandler.handleReceivedRequest(request);

    dispatchResponseToRequest(clientSocket, request, response);
  }


  protected void dispatchResponseToRequest(Socket clientSocket, Request request, Response response) {
    try {
      byte[] serializedResponse = messageSerializer.serializeResponse(response);
      SocketResult result = socketHandler.sendMessage(clientSocket, serializedResponse);

      if(result.isSuccessful()) {

      }
      // TODO: what to do in error case?
    } catch(Exception e) {
      log.error("Could not send response " + response + " to request " + request + " to client " + (clientSocket != null ? clientSocket.getInetAddress() : ""), e);
    }
  }

}
