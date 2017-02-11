package net.dankito.sync.synchronization.files;


import net.dankito.sync.FileSyncEntity;
import net.dankito.sync.SyncJobItem;
import net.dankito.sync.SyncModuleConfiguration;
import net.dankito.sync.SyncState;
import net.dankito.sync.persistence.IEntityManager;
import net.dankito.utils.AsyncProducerConsumerQueue;
import net.dankito.utils.ConsumerListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.inject.Named;


@Named
public class FileSyncService {

  private static final Logger log = LoggerFactory.getLogger(FileSyncService.class);


  protected IEntityManager entityManager;

  protected Thread listenerThread;

  protected ServerSocket listenerSocket;

  protected boolean isReceivingFilesEnabled = false;

  protected int listenerPort = FileSyncServiceDefaultConfig.FILE_SYNC_SERVICE_DEFAULT_LISTENER_PORT;

  protected int bufferSize = FileSyncServiceDefaultConfig.DEFAULT_BUFFER_SIZE;

  protected Map<String, SyncJobItem> currentFileSyncJobItems = new ConcurrentHashMap<>();

  protected List<FileSyncListener> fileSyncListeners = new CopyOnWriteArrayList<>();

  protected AsyncProducerConsumerQueue<Socket> connectedClients;



  public FileSyncService(IEntityManager entityManager) {
    this.entityManager = entityManager;

    connectedClients = new AsyncProducerConsumerQueue<Socket>(FileSyncServiceDefaultConfig.COUNT_PARALLEL_FILE_TRANSFERS, connectedClientsListener);
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


  public void start() {
    startListenerSocketInNewThread();
  }

  protected void startListenerSocketInNewThread() {
    if(listenerThread == null) {
      listenerThread = new Thread(new Runnable() {
        @Override
        public void run() {
          startListenerSocket();
        }
      });

      listenerThread.start();
    }
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

        clientSocket.setSoTimeout(0);
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
    InputStream clientInputStream = null;
    DataInputStream clientDataInputStream = null;

    try {
      clientInputStream = new BufferedInputStream(clientSocket.getInputStream());
      clientDataInputStream = new DataInputStream(clientInputStream);

      String syncJobItemId = clientDataInputStream.readUTF();
      SyncJobItem jobItem = getFileSyncJobItemForId(syncJobItemId);

      if(jobItem != null) {
        File destinationFile = getFileDestinationPathForSyncJobItem(jobItem);
        destinationFile.getParentFile().mkdirs();

        if(receiveFile(clientInputStream, destinationFile, jobItem, clientSocket)) {
          jobItem.getEntity().setLocalLookupKey(destinationFile.getAbsolutePath());
          removeFileSyncJobItem(jobItem);
          callFileRetrievedListeners(jobItem, destinationFile);
        }
        else { // TODO: what to do when receiving file fails?
          log.error("Failed receiving file for SyncJobItem " + jobItem + " and writing it to " + destinationFile);
          try { destinationFile.delete(); } catch(Exception e) { log.error("Could not deleted failed file " + destinationFile, e); }
        }
      }
    } catch(Exception e) {
      log.error("Could not receive file from client " + (clientSocket != null ? clientSocket.getInetAddress() : ""), e);
    }
    finally {
      closeSocketAndStreams(clientSocket, clientInputStream, clientDataInputStream);
    }
  }

  protected boolean receiveFile(InputStream inputStream, File destinationFile, SyncJobItem jobItem, Socket clientSocket) throws IOException {
    long startTime = System.currentTimeMillis();

    OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(destinationFile));

    byte[] buffer = new byte[bufferSize];
    int read;
    int totalRead = 0;

    while ((read = inputStream.read(buffer)) != -1) {
      outputStream.write(buffer, 0, read);
      totalRead += read;
    }

    long endTime = System.currentTimeMillis();
    log.info(totalRead + " of " + jobItem.getDataSize() + " bytes read from " + clientSocket.getInetAddress() + " in " + (endTime - startTime) + " ms.");

    return totalRead == jobItem.getDataSize();
  }

  protected void closeSocketAndStreams(Socket clientSocket, InputStream clientInputStream, DataInputStream clientDataInputStream) {
    if(clientDataInputStream != null) {
      try { clientDataInputStream.close(); } catch(Exception e) { log.warn("Could not close clientDataInputStream", e); }
    }

    if(clientInputStream != null) {
      try { clientInputStream.close(); } catch(Exception e) { log.warn("Could not close clientInputStream", e); }
    }

    try { clientSocket.close(); } catch(Exception e) { log.warn("Could not close clientSocket", e); }
  }


  protected File getFileDestinationPathForSyncJobItem(SyncJobItem jobItem) {
    FileSyncEntity fileSyncEntity = (FileSyncEntity)jobItem.getEntity();
    SyncModuleConfiguration syncModuleConfiguration = jobItem.getSyncModuleConfiguration();

    return getFileDestinationPath(syncModuleConfiguration.getSourcePath(), syncModuleConfiguration.getDestinationPath(), fileSyncEntity.getFilePath());
  }

  protected File getFileDestinationPath(String synchronizationSourceRootFolder, String synchronizationDestinationRootFolder, String entitySourcePathString) {
    File entityRelativeSourcePath = getFileRelativePath(synchronizationSourceRootFolder, entitySourcePathString);

    return new File(synchronizationDestinationRootFolder, entityRelativeSourcePath.getPath());
  }

  protected File getFileRelativePath(String rootFolderString, String filePathString) {
    File rootFolder = new File(rootFolderString);
    File filePath = new File(filePathString);

    File fileRelativePath = new File(filePath.getName());

    File parent = filePath.getParentFile();

    while(parent != null && rootFolder.compareTo(parent) < 0) {
      fileRelativePath = new File(parent.getName(), fileRelativePath.getPath());
      parent = parent.getParentFile();
    }

    log.info("Determined relative path for " + filePathString + " in folder " + rootFolderString + " is " + fileRelativePath);

    return fileRelativePath;
  }


  public void fileSyncJobItemRetrieved(SyncJobItem jobItem) {
    currentFileSyncJobItems.put(jobItem.getId(), jobItem);

    jobItem.setState(SyncState.TRANSFERRING_FILE_TO_DESTINATION_DEVICE); // TODO: this is actually bad architecture setting state outside SyncConfigurationManager
    entityManager.updateEntity(jobItem);
  }

  protected void removeFileSyncJobItem(SyncJobItem jobItem) {
    currentFileSyncJobItems.remove(jobItem.getId());
  }

  public SyncJobItem getFileSyncJobItemForId(String syncJobItemId) {
    return currentFileSyncJobItems.get(syncJobItemId);
  }


  public boolean addFileSyncListener(FileSyncListener listener) {
    return fileSyncListeners.add(listener);
  }

  public boolean removeFileSyncListener(FileSyncListener listener) {
    return fileSyncListeners.remove(listener);
  }

  protected void callFileRetrievedListeners(SyncJobItem jobItem, File destinationFile) {
    RetrievedFile retrievedFile = new RetrievedFile(jobItem, destinationFile);

    for(FileSyncListener listener : fileSyncListeners) {
      listener.fileRetrieved(retrievedFile);
    }
  }

}
