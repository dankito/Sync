package net.dankito.sync;

import net.dankito.sync.config.DatabaseTableConfig;

import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

@Entity(name = DatabaseTableConfig.SYNC_ENTITY_TABLE_NAME)
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = DatabaseTableConfig.SYNC_ENTITY_DISCRIMINATOR_COLUMN_NAME, discriminatorType = DiscriminatorType.STRING, length = 20)
public abstract class SyncEntity extends BaseEntity {

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = DatabaseTableConfig.SYNC_ENTITY_SYNC_MODULE_CONFIGURATION_JOIN_COLUMN_NAME)
  protected SyncModuleConfiguration syncModuleConfiguration;

  @ManyToOne
  @JoinColumn(name = DatabaseTableConfig.SYNC_ENTITY_SOURCE_DEVICE_ID_JOIN_COLUMN_NAME)
  protected Device sourceDevice;

  @Column(name = DatabaseTableConfig.SYNC_ENTITY_LOOK_UP_KEY_ON_SOURCE_DEVICE_COLUMN_NAME)
  protected String lookUpKeyOnSourceDevice;


  protected SyncEntity() { // for reflection

  }

  public SyncEntity(SyncModuleConfiguration syncModuleConfiguration) {
    this.syncModuleConfiguration = syncModuleConfiguration;
  }


  public SyncModuleConfiguration getSyncModuleConfiguration() {
    return syncModuleConfiguration;
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

}
