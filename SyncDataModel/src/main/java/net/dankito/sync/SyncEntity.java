package net.dankito.sync;

public abstract class SyncEntity extends BaseEntity {

  protected SyncModuleConfiguration syncModuleConfiguration;

  protected String sourceDeviceId;

  protected String idOnSourceDevice;


  public SyncEntity(SyncModuleConfiguration syncModuleConfiguration) {
    this.syncModuleConfiguration = syncModuleConfiguration;
  }


  public SyncModuleConfiguration getSyncModuleConfiguration() {
    return syncModuleConfiguration;
  }

  public String getSourceDeviceId() {
    return sourceDeviceId;
  }

  public void setSourceDeviceId(String sourceDeviceId) {
    this.sourceDeviceId = sourceDeviceId;
  }

  public String getIdOnSourceDevice() {
    return idOnSourceDevice;
  }

  public void setIdOnSourceDevice(String idOnSourceDevice) {
    this.idOnSourceDevice = idOnSourceDevice;
  }

}
