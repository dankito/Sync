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

@Entity(name = DatabaseTableConfig.SYNC_ENTITY_TABLE_NAME)
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = DatabaseTableConfig.SYNC_ENTITY_DISCRIMINATOR_COLUMN_NAME, discriminatorType = DiscriminatorType.STRING, length = 20)
public abstract class SyncEntity extends BaseEntity {

  @ManyToOne
  @JoinColumn(name = DatabaseTableConfig.SYNC_ENTITY_SOURCE_DEVICE_ID_JOIN_COLUMN_NAME)
  protected Device sourceDevice;

  @Column(name = DatabaseTableConfig.SYNC_ENTITY_LOOK_UP_KEY_ON_SOURCE_DEVICE_COLUMN_NAME)
  protected String lookUpKeyOnSourceDevice;

  @Temporal(TemporalType.TIMESTAMP)
  @Column(name = DatabaseTableConfig.SYNC_ENTITY_CREATED_ON_DEVICE_COLUMN_NAME)
  protected Date createdOnDevice;

  @Temporal(TemporalType.TIMESTAMP)
  @Column(name = DatabaseTableConfig.SYNC_ENTITY_LAST_MODIFIED_ON_DEVICE_COLUMN_NAME)
  protected Date lastModifiedOnDevice;


  public SyncEntity() {

  }


  public Device getSourceDevice() {
    return sourceDevice;
  }

  public String getLookUpKeyOnSourceDevice() {
    return lookUpKeyOnSourceDevice;
  }

  public void setLookUpKeyOnSourceDevice(String lookUpKeyOnSourceDevice) {
    this.lookUpKeyOnSourceDevice = lookUpKeyOnSourceDevice;
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
