package net.dankito.sync;

public abstract class SyncEntity extends BaseEntity {

  protected String sourceDeviceId;

  protected String idOnSourceDevice;


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
