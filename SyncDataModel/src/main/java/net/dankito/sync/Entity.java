package net.dankito.sync;

import java.util.Date;

public abstract class Entity {

  protected String id;

  protected String sourceDeviceId;

  protected String idOnSourceDevice;

  protected Date createdOn;

  protected Date modifiedOn;

  protected boolean deleted;


  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
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

  public Date getCreatedOn() {
    return createdOn;
  }

  public void setCreatedOn(Date createdOn) {
    this.createdOn = createdOn;
  }

  public Date getModifiedOn() {
    return modifiedOn;
  }

  public void setModifiedOn(Date modifiedOn) {
    this.modifiedOn = modifiedOn;
  }

  public boolean isDeleted() {
    return deleted;
  }

  public void setDeleted(boolean deleted) {
    this.deleted = deleted;
  }

}
