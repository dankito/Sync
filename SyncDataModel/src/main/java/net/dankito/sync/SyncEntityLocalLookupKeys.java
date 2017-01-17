package net.dankito.sync;

import net.dankito.sync.config.DatabaseTableConfig;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;


@Entity(name = DatabaseTableConfig.SYNC_ENTITY_LOCAL_LOOKUP_KEYS_TABLE_NAME)
public class SyncEntityLocalLookupKeys extends BaseEntity {

  @Column(name = DatabaseTableConfig.SYNC_ENTITY_LOCAL_LOOK_UP_KEYS_ENTITY_TYPE_COLUMN_NAME)
  protected String entityType;

  @Column(name = DatabaseTableConfig.SYNC_ENTITY_LOCAL_LOOK_UP_KEYS_ENTITY_DATABASE_ID_COLUMN_NAME)
  protected String entityDatabaseId;

  @Column(name = DatabaseTableConfig.SYNC_ENTITY_LOCAL_LOOK_UP_KEYS_ENTITY_LOCAL_LOOK_UP_KEY_COLUMN_NAME)
  protected String entityLocalLookUpKey;

  @Temporal(TemporalType.TIMESTAMP)
  @Column(name = DatabaseTableConfig.SYNC_ENTITY_LOCAL_LOOK_UP_KEYS_ENTITY_LAST_MODIFIED_ON_DEVICE_COLUMN_NAME)
  protected Date entityLastModifiedOnDevice;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = DatabaseTableConfig.SYNC_ENTITY_LOCAL_LOOK_UP_KEYS_SYNC_MODULE_CONFIGURATION_JOIN_COLUMN_NAME)
  protected SyncModuleConfiguration syncModuleConfiguration;


  protected SyncEntityLocalLookupKeys() { // for reflection

  }

  public SyncEntityLocalLookupKeys(String entityType, String entityDatabaseId, String entityLocalLookUpKey, Date entityLastModifiedOnDevice, SyncModuleConfiguration syncModuleConfiguration) {
    this.entityType = entityType;
    this.entityDatabaseId = entityDatabaseId;
    this.entityLocalLookUpKey = entityLocalLookUpKey;
    this.entityLastModifiedOnDevice = entityLastModifiedOnDevice;
    this.syncModuleConfiguration = syncModuleConfiguration;
  }


  public String getEntityType() {
    return entityType;
  }

  public String getEntityDatabaseId() {
    return entityDatabaseId;
  }

  public String getEntityLocalLookUpKey() {
    return entityLocalLookUpKey;
  }

  public void setEntityLocalLookUpKey(String entityLocalLookUpKey) {
    this.entityLocalLookUpKey = entityLocalLookUpKey;
  }

  public Date getEntityLastModifiedOnDevice() {
    return entityLastModifiedOnDevice;
  }

  public void setEntityLastModifiedOnDevice(Date entityLastModifiedOnDevice) {
    this.entityLastModifiedOnDevice = entityLastModifiedOnDevice;
  }

  public SyncModuleConfiguration getSyncModuleConfiguration() {
    return syncModuleConfiguration;
  }


  @Override
  public String toString() {
    return getEntityType() + ": " + getEntityLocalLookUpKey();
  }

}
