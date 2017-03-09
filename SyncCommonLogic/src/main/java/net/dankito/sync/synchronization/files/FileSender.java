package net.dankito.sync.synchronization.files;


import net.dankito.utils.AsyncProducerConsumerQueue;
import net.dankito.utils.ConsumerListener;
import net.dankito.utils.IThreadPool;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

import javax.inject.Named;


@Named
public class FileSender {

  private static final Logger log = LoggerFactory.getLogger(FileSender.class);


  protected IThreadPool threadPool;

  protected AsyncProducerConsumerQueue<FileSyncJobItem> queuedJobs;

  protected int bufferSize = FileSyncServiceDefaultConfig.DEFAULT_BUFFER_SIZE;


  public FileSender() {
    this.queuedJobs = new AsyncProducerConsumerQueue<>(FileSyncServiceDefaultConfig.COUNT_PARALLEL_FILE_TRANSFERS, fileSyncJobItemConsumerListener);
  }


  protected ConsumerListener fileSyncJobItemConsumerListener = new ConsumerListener<FileSyncJobItem>() {
    @Override
    public void consumeItem(FileSyncJobItem item) {
      sendFile(item);
    }
  };

  public void sendFileAsync(final FileSyncJobItem jobItem) {
    queuedJobs.add(jobItem);
  }

  protected void sendFile(FileSyncJobItem jobItem) {
    FileInputStream fileInputStream = null;
    Socket socket = null;
    OutputStream socketOutputStream = null;
    DataOutputStream dataOutputStream = null;

    try {
      fileInputStream = new FileInputStream(jobItem.getFilePath());

      socket = new Socket();
      socket.connect(new InetSocketAddress(jobItem.getDestinationAddress(), jobItem.getDestinationPort()), 0);
      socketOutputStream = new BufferedOutputStream(socket.getOutputStream());

      dataOutputStream = new DataOutputStream(socketOutputStream);
      dataOutputStream.writeUTF(jobItem.getSyncJobItem().getId());

      sendFileToDestination(fileInputStream, socketOutputStream, jobItem);
    } catch (Exception e) {
      log.error("Could not send file " + jobItem.getFilePath() + " to " + jobItem.getDestinationAddress() + " on port " + jobItem.getDestinationPort(), e);
    }
    finally {
      closeSocketAndStreams(socket, fileInputStream, socketOutputStream, dataOutputStream);
    }
  }

  protected boolean sendFileToDestination(FileInputStream fileInputStream, OutputStream socketOutputStream, FileSyncJobItem jobItem) throws IOException {
    long startTime = System.currentTimeMillis();

    byte[] buffer = new byte[bufferSize];
    int read;
    int sendTotal = 0;

    while ((read = fileInputStream.read(buffer)) != -1) {
      socketOutputStream.write(buffer, 0, read);
      sendTotal += read;
    }

    try { socketOutputStream.flush(); } catch(Exception ignored) { }

    long endTime = System.currentTimeMillis();
    log.info(sendTotal + " of " + jobItem.getSyncJobItem().getDataSize() + " bytes written to " + jobItem.getDestinationAddress() + " in " + (endTime - startTime) + " ms.");

    return sendTotal == jobItem.getSyncJobItem().getDataSize();
  }

  protected void closeSocketAndStreams(Socket socket, FileInputStream fileInputStream, OutputStream socketOutputStream, DataOutputStream dataOutputStream) {
    if(fileInputStream != null) {
      try { fileInputStream.close(); } catch(Exception e) { log.warn("Could not close fileInputStream", e); }
    }

    if(dataOutputStream != null) {
      try { dataOutputStream.close(); } catch(Exception e) { log.warn("Could not close dataOutputStream", e); }
    }

    if(socketOutputStream != null) {
      try { socketOutputStream.close(); } catch(Exception e) { log.warn("Could not close socketOutputStream", e); }
    }

    if(socket != null) {
      try { socket.close(); } catch (Exception e) { log.warn("Could not close socket", e); }
    }
  }

}
