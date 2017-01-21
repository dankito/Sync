package net.dankito.sync.synchronization.files;

import net.dankito.sync.SyncJobItem;


public class FileSyncJobItem {

  protected SyncJobItem syncJobItem;

  protected String filePath;

  protected String destinationAddress;

  protected int destinationPort;


  public FileSyncJobItem(SyncJobItem syncJobItem, String filePath, String destinationAddress, int destinationPort) {
    this.syncJobItem = syncJobItem;
    this.filePath = filePath;
    this.destinationAddress = destinationAddress;
    this.destinationPort = destinationPort;
  }


  public SyncJobItem getSyncJobItem() {
    return syncJobItem;
  }

  public String getFilePath() {
    return filePath;
  }

  public String getDestinationAddress() {
    return destinationAddress;
  }

  public int getDestinationPort() {
    return destinationPort;
  }

}
