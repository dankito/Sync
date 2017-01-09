package net.dankito.sync;

import net.dankito.sync.config.DatabaseTableConfig;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;


@Entity(name = DatabaseTableConfig.SYNC_JOB_ITEM_TABLE_NAME)
public class SyncJobItem extends BaseEntity {

  @ManyToOne
  @JoinColumn(name = DatabaseTableConfig.SYNC_JOB_ITEM_SYNC_MODULE_CONFIGURATION_JOIN_COLUMN_NAME)
  protected SyncModuleConfiguration syncModuleConfiguration;

  @ManyToOne
  @JoinColumn(name = DatabaseTableConfig.SYNC_JOB_ITEM_SYNC_ENTITY_JOIN_COLUMN_NAME)
  protected SyncEntity entity;

  @Enumerated(EnumType.ORDINAL)
  @Column(name = DatabaseTableConfig.SYNC_JOB_ITEM_STATE_COLUMN_NAME)
  protected SyncState state;

  @Temporal(value = TemporalType.TIMESTAMP)
  @Column(name = DatabaseTableConfig.SYNC_JOB_ITEM_START_TIME_COLUMN_NAME)
  protected Date startTime;

  @Temporal(value = TemporalType.TIMESTAMP)
  @Column(name = DatabaseTableConfig.SYNC_JOB_ITEM_FINISH_TIME_COLUMN_NAME)
  protected Date finishTime;


  protected SyncJobItem() { // for reflection

  }

  public SyncJobItem(SyncModuleConfiguration config, SyncEntity entity) {
    this.syncModuleConfiguration = config;
    this.entity = entity;

    this.startTime = new Date();
    this.state = SyncState.INITIALIZED;
  }


  public SyncModuleConfiguration getSyncModuleConfiguration() {
    return syncModuleConfiguration;
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
