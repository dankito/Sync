package net.dankito.sync.synchronization;

import net.dankito.sync.ISyncModule;
import net.dankito.sync.SyncEntity;


public class SyncEntityChange {

  protected ISyncModule syncModule;

  protected SyncEntity syncEntity;


  public SyncEntityChange(ISyncModule syncModule, SyncEntity syncEntity) {
    this.syncModule = syncModule;
    this.syncEntity = syncEntity;
  }


  public ISyncModule getSyncModule() {
    return syncModule;
  }

  public SyncEntity getSyncEntity() {
    return syncEntity;
  }


  @Override
  public String toString() {
    return getSyncModule() + ": " + getSyncEntity();
  }

}
