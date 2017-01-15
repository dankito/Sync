package net.dankito.sync.synchronization.modules;


public enum SyncModuleDefaultTypes {

  Contacts("Contacts"),
  CallLog("CallLog"),
  AndroidInternalPhotos("AndroidInternalPhotos"),
  AndroidExternalPhotos("AndroidExternalPhotos");


  private String typeName;

  SyncModuleDefaultTypes(String typeName) {
    this.typeName = typeName;
  }


  public String getTypeName() {
    return typeName;
  }


  @Override
  public String toString() {
    return getTypeName();
  }

}
