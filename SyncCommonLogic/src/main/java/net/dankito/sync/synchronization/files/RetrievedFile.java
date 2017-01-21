package net.dankito.sync.synchronization.files;

import net.dankito.sync.SyncJobItem;

import java.io.File;


public class RetrievedFile {

  protected SyncJobItem jobItem;

  protected File destinationFile;


  public RetrievedFile(SyncJobItem jobItem, File destinationFile) {
    this.jobItem = jobItem;
    this.destinationFile = destinationFile;
  }


  public SyncJobItem getJobItem() {
    return jobItem;
  }

  public File getDestinationFile() {
    return destinationFile;
  }


  @Override
  public String toString() {
    return getJobItem() + ": " + destinationFile;
  }

}
