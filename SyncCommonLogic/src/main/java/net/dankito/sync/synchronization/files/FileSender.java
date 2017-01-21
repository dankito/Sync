package net.dankito.sync.synchronization.files;


import net.dankito.utils.IThreadPool;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;


public class FileSender {

  private static final Logger log = LoggerFactory.getLogger(FileSender.class);


  protected IThreadPool threadPool;

  protected int bufferSize = FileSyncServiceDefaultConfig.DEFAULT_BUFFER_SIZE;


  public FileSender(IThreadPool threadPool) {
    this.threadPool = threadPool;
  }


  public void sendFileAsync(final FileSyncJobItem jobItem) {
    threadPool.runAsync(new Runnable() {
      @Override
      public void run() {
        sendFile(jobItem);
      }
    });
  }

  protected void sendFile(FileSyncJobItem jobItem) {
    try {
      FileInputStream fileInputStream = new FileInputStream(jobItem.getFilePath());

      Socket socket = new Socket(jobItem.getDestinationAddress(), jobItem.getDestinationPort());
      OutputStream socketOutputStream = socket.getOutputStream();

      DataOutputStream dos = new DataOutputStream(socketOutputStream);
      dos.writeUTF(jobItem.getSyncJobItem().getId());

      sendFileToDestination(fileInputStream, socketOutputStream, jobItem);

      socketOutputStream.close();
      fileInputStream.close();
      socket.close();
    } catch (Exception e) {
      log.error("Could not send file " + jobItem.getFilePath() + " to " + jobItem.getDestinationAddress() + " on port " + jobItem.getDestinationPort(), e);
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

    long endTime = System.currentTimeMillis();
    log.info(sendTotal + " bytes written to " + jobItem.getDestinationAddress() + " in " + (endTime - startTime) + " ms.");

    return sendTotal == jobItem.getSyncJobItem().getDataSize();
  }

}
