package net.dankito.sync;

import java.util.Date;

/**
 * Created by ganymed on 07/01/17.
 */
public abstract class BaseEntity {

  protected String id;

  protected Date createdOn;

  protected Date modifiedOn;

  protected boolean deleted;


  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
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
