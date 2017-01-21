package net.dankito.sync.synchronization.modules;


import net.dankito.sync.SyncJobItem;

public class HandleRetrievedSynchronizedEntityResult {

  protected SyncJobItem syncJobItem;

  protected boolean successful;

  protected boolean hasPermissionsForOperation;


  public HandleRetrievedSynchronizedEntityResult(SyncJobItem syncJobItem, boolean successful) {
    this(syncJobItem, successful, true);
  }

  public HandleRetrievedSynchronizedEntityResult(SyncJobItem syncJobItem, boolean successful, boolean hasPermissionsForOperation) {
    this.syncJobItem = syncJobItem;
    this.successful = successful;
    this.hasPermissionsForOperation = hasPermissionsForOperation;
  }


  public SyncJobItem getSyncJobItem() {
    return syncJobItem;
  }

  public boolean isSuccessful() {
    return successful;
  }

  public boolean isHasPermissionsForOperation() {
    return hasPermissionsForOperation;
  }


  @Override
  public String toString() {
    return getSyncJobItem() + ": successful ? " + isSuccessful();
  }

}
