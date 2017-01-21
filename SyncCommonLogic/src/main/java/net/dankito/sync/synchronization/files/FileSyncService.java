package net.dankito.sync.synchronization.files;


import net.dankito.sync.SyncJobItem;
import net.dankito.utils.AsyncProducerConsumerQueue;
import net.dankito.utils.ConsumerListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


public class FileSyncService {

  private static final Logger log = LoggerFactory.getLogger(FileSyncService.class);


  protected Thread listenerThread;

  protected ServerSocket listenerSocket;

  protected boolean isReceivingFilesEnabled = false;

  protected int listenerPort = FileSyncServiceDefaultConfig.FILE_SYNC_SERVICE_DEFAULT_LISTENER_PORT;

  protected int bufferSize = FileSyncServiceDefaultConfig.DEFAULT_BUFFER_SIZE;

  protected Map<String, SyncJobItem> currentFileSyncJobItems = new ConcurrentHashMap<>();

  protected AsyncProducerConsumerQueue<Socket> connectedClients;


  public FileSyncService() {
    connectedClients = new AsyncProducerConsumerQueue<Socket>(1, connectedClientsListener);
    startListenerSocketInNewThread();
  }


  public void stop() {
    stopListener();
  }


  protected void stopListener() {
    isReceivingFilesEnabled = false;

    if(listenerSocket != null) {
      try { listenerSocket.close(); } catch(Exception e) { log.error("Could not close listenerSocket", e); }
      listenerSocket = null;
    }

    if(listenerThread != null) {
      try { listenerThread.join(100); } catch(Exception e) { log.error("Could not stop listenerThread", e); }
      listenerThread = null;
    }

    connectedClients.stop();
  }

  protected void startListenerSocketInNewThread() {
    listenerThread = new Thread(new Runnable() {
      @Override
      public void run() {
        startListenerSocket();
      }
    });

    listenerThread.start();
  }

  protected void startListenerSocket() {
    try {
      listenerSocket = new ServerSocket();
      listenerSocket.setReuseAddress(true); // TODO: really?
      listenerSocket.bind(new InetSocketAddress(listenerPort));

      isReceivingFilesEnabled = true;

      waitForArrivingPackets();
    } catch(Exception e) {
      log.error("Could not start listenerSocket", e);
      stopListener(); // TODO: but what to do then?
    }
  }

  protected void waitForArrivingPackets() {
    while(isReceivingFilesEnabled) {
      try {
        Socket clientSocket = listenerSocket.accept();
        System.out.println("Got a connection on port " + listenerPort + " from " + clientSocket.getInetAddress());

        connectedClients.add(clientSocket); // get them off this thread read so that listenerSocket won't be blocked during receiving file
      }
      catch (Exception e) {
        log.error("Error occurred while waiting for client connections", e);
      }
    }
  }


  protected ConsumerListener<Socket> connectedClientsListener = new ConsumerListener<Socket>() {
    @Override
    public void consumeItem(Socket item) {
      receiveFileFromClient(item);
    }
  };

  protected void receiveFileFromClient(Socket clientSocket) {
    try {
      long startTime = System.currentTimeMillis();

      InputStream clientInputStream = clientSocket.getInputStream();
      DataInputStream clientDataInputStream = new DataInputStream(clientInputStream);

      String syncJobItemId = clientDataInputStream.readUTF();
      SyncJobItem jobItem = getFileSyncJobItemForId(syncJobItemId);

      if(jobItem != null) {
        int totalRead = receiveFile(clientDataInputStream, syncJobItemId);

        long endTime = System.currentTimeMillis();
        log.info(totalRead + " bytes read from " + clientSocket.getInetAddress() + " in " + (endTime - startTime) + " ms.");

        removeFileSyncJobItem(jobItem);
      }

      clientInputStream.close();
      clientSocket.close();
    } catch(Exception e) {
      log.error("Could not receive file from client " + (clientSocket != null ? clientSocket.getInetAddress() : ""), e);
    }
  }

  protected int receiveFile(DataInputStream clientDataInputStream, String syncJobItemId) throws IOException {
    String destinationFile = getDestinationFilePathForSyncJobItem(syncJobItemId);
    OutputStream output = new FileOutputStream(destinationFile);

    byte[] buffer = new byte[bufferSize];
    int read;
    int totalRead = 0;

    while ((read = clientDataInputStream.read(buffer)) != -1) {
      output.write(buffer, 0, read);
      totalRead += read;
    }

    return totalRead;
  }

  protected String getDestinationFilePathForSyncJobItem(String syncJobItemId) throws IOException {
    return File.createTempFile("received_file_" + syncJobItemId, ".tmp").getAbsolutePath();
  }


  public void fileSyncJobItemRetrieved(SyncJobItem jobItem) {
    currentFileSyncJobItems.put(jobItem.getId(), jobItem);
  }

  protected void removeFileSyncJobItem(SyncJobItem jobItem) {
    currentFileSyncJobItems.remove(jobItem.getId());
  }

  public SyncJobItem getFileSyncJobItemForId(String syncJobItemId) {
    return currentFileSyncJobItems.get(syncJobItemId);
  }

}
