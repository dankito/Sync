package net.dankito.sync;

/**
 * Created by ganymed on 07/01/17.
 */

public class SyncModuleConfiguration extends BaseEntity {

  protected Class syncModule;

  protected boolean isBiDirectional;

  protected String destinationPath; // for FileSyncEntities


  public Class getSyncModule() {
    return syncModule;
  }

  public void setSyncModule(Class syncModule) {
    this.syncModule = syncModule;
  }

  public boolean isBiDirectional() {
    return isBiDirectional;
  }

  public void setBiDirectional(boolean biDirectional) {
    isBiDirectional = biDirectional;
  }

  public String getDestinationPath() {
    return destinationPath;
  }

  public void setDestinationPath(String destinationPath) {
    this.destinationPath = destinationPath;
  }

}
