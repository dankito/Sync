package net.dankito.sync.synchronization;

import net.dankito.sync.SyncEntity;
import net.dankito.sync.synchronization.modules.ISyncModule;


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
