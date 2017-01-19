package net.dankito.sync.synchronization.modules;


public enum SyncModuleDefaultTypes {

  CONTACTS("Contacts"),
  CALL_LOG("CallLog"),
  ANDROID_INTERNAL_PHOTOS("AndroidInternalPhotos"),
  ANDROID_EXTERNAL_PHOTOS("AndroidExternalPhotos");


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
