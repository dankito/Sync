package net.dankito.sync.communication;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;


public class SocketHandler {

  private static final Logger log = LoggerFactory.getLogger(SocketHandler.class);


  public SocketResult sendMessage(Socket socket, byte[] message) {
    InputStream inputStream = null;
    OutputStream outputStream = null;

    try {
      inputStream = new ByteArrayInputStream(message);
      outputStream = socket.getOutputStream();

      return sendMessage(inputStream, outputStream);
    }
    catch(Exception e) {
      log.error("Could not send message to client " + (socket != null ? socket.getInetAddress() : ""), e);
      return new SocketResult(e);
    }
    finally {
      if(inputStream != null) {
        try { inputStream.close(); } catch(Exception e) { log.warn("Could not close input stream", e); }
      }

      if(outputStream != null) {
        try { outputStream.flush(); } catch(Exception e) { log.warn("Could not flush output stream", e); }
        // do not close outputStream, otherwise socket gets closed as well
      }
    }
  }

  protected SocketResult sendMessage(InputStream inputStream, OutputStream outputStream) throws IOException {
    byte[] buffer = new byte[CommunicationConfig.BUFFER_SIZE];
    int read;

    while((read = inputStream.read(buffer, 0, buffer.length)) > 0) {
      outputStream.write(buffer, 0, read);
    }

    return new SocketResult(true);
  }


  public SocketResult receiveMessage(Socket socket) {
    try {
      InputStream inputStream = socket.getInputStream();

      // do not close inputStream, otherwise socket gets closed
      return receiveMessage(inputStream);
    } catch(Exception e) {
      log.error("Could not receive response", e);

      return new SocketResult(e);
    }
  }

  protected SocketResult receiveMessage(InputStream inputStream) throws IOException {
    byte[] buffer = new byte[CommunicationConfig.MAX_MESSAGE_SIZE];

    int read = 0;
    int totalRead = 0;

    while((read = inputStream.read(buffer, totalRead, CommunicationConfig.BUFFER_SIZE)) > -1) {
      totalRead += read;

      if(read < CommunicationConfig.BUFFER_SIZE || totalRead >= CommunicationConfig.MAX_MESSAGE_SIZE) {
        break;
      }
    }

    if(totalRead > 0 && totalRead < CommunicationConfig.MAX_MESSAGE_SIZE) {
      String responseString = new String(buffer, 0, totalRead, CommunicationConfig.MESSAGE_CHARSET_NAME);
      buffer = null; // TODO: run GarbageCollector to free memory?

      return new SocketResult(responseString);
    }
    else {
      if(totalRead <= 0) {
        return new SocketResult(new Exception("Could not receive any bytes"));
      }
      else {
        return new SocketResult(new Exception("Received message exceeds max message length of " + CommunicationConfig.MAX_MESSAGE_SIZE));
      }
    }
  }

  public void closeSocket(Socket socket) {
    if(socket != null) {
      try {
        socket.close();
      } catch(Exception e) { log.error("Could not close socket", e); }
    }
  }

}
