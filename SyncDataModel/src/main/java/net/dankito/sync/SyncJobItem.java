package net.dankito.sync;

import java.util.Date;

/**
 * Created by ganymed on 07/01/17.
 */

public class SyncJobItem extends BaseEntity {

  protected SyncEntity entity;

  protected SyncState state;

  protected Date startTime;

  protected Date finishTime;


  public SyncJobItem(SyncEntity entity) {
    this.entity = entity;
    this.state = SyncState.INITIALIZED;
  }


  public SyncEntity getEntity() {
    return entity;
  }

  public SyncState getState() {
    return state;
  }

  public void setState(SyncState state) {
    this.state = state;
  }

  public Date getStartTime() {
    return startTime;
  }

  public void setStartTime(Date startTime) {
    this.startTime = startTime;
  }

  public Date getFinishTime() {
    return finishTime;
  }

  public void setFinishTime(Date finishTime) {
    this.finishTime = finishTime;
  }


  @Override
  public String toString() {
    return getState() + " " + getEntity();
  }

}
