package net.dankito.sync;

import net.dankito.sync.config.DatabaseTableConfig;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

@Entity(name = DatabaseTableConfig.SYNC_ENTITY_TABLE_NAME)
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = DatabaseTableConfig.SYNC_ENTITY_DISCRIMINATOR_COLUMN_NAME, discriminatorType = DiscriminatorType.STRING, length = 20)
public abstract class SyncEntity extends BaseEntity {

  @ManyToOne
  @JoinColumn(name = DatabaseTableConfig.SYNC_ENTITY_SOURCE_DEVICE_ID_JOIN_COLUMN_NAME)
  protected Device sourceDevice;

  @Transient
  protected String localLookupKey;

  @Temporal(TemporalType.TIMESTAMP)
  @Column(name = DatabaseTableConfig.SYNC_ENTITY_CREATED_ON_DEVICE_COLUMN_NAME)
  protected Date createdOnDevice;

  @Transient
  protected Date lastModifiedOnDevice;


  public SyncEntity() {

  }


  public Device getSourceDevice() {
    return sourceDevice;
  }

  public void setSourceDevice(Device sourceDevice) {
    this.sourceDevice = sourceDevice;
  }

  public String getLocalLookupKey() {
    return localLookupKey;
  }

  public void setLocalLookupKey(String localLookupKey) {
    this.localLookupKey = localLookupKey;
  }

  public Date getCreatedOnDevice() {
    return createdOnDevice;
  }

  public void setCreatedOnDevice(Date createdOnDevice) {
    this.createdOnDevice = createdOnDevice;
  }

  public Date getLastModifiedOnDevice() {
    return lastModifiedOnDevice;
  }

  public void setLastModifiedOnDevice(Date lastModifiedOnDevice) {
    this.lastModifiedOnDevice = lastModifiedOnDevice;
  }

}
