package net.dankito.sync.synchronization.modules;


public abstract class SyncModuleBase implements ISyncModule {

  public String getModuleUniqueKey() {
    return getClass().getName();
  }

}
