package net.dankito.sync;

import net.dankito.sync.config.DatabaseTableConfig;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;


@Entity(name = DatabaseTableConfig.SYNC_ENTITY_LOCAL_LOOK_UP_KEYS_TABLE_NAME)
public class SyncEntityLocalLookUpKeys extends BaseEntity {

  @Column(name = DatabaseTableConfig.SYNC_ENTITY_LOCAL_LOOK_UP_KEYS_ENTITY_TYPE_COLUMN_NAME)
  protected String entityType;

  @Column(name = DatabaseTableConfig.SYNC_ENTITY_LOCAL_LOOK_UP_KEYS_ENTITY_DATABASE_ID_COLUMN_NAME)
  protected String entityDatabaseId;

  @Column(name = DatabaseTableConfig.SYNC_ENTITY_LOCAL_LOOK_UP_KEYS_ENTITY_LOCAL_LOOK_UP_KEY_COLUMN_NAME)
  protected String entityLocalLookUpKey;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = DatabaseTableConfig.SYNC_ENTITY_LOCAL_LOOK_UP_KEYS_SYNC_MODULE_CONFIGURATION_JOIN_COLUMN_NAME)
  protected SyncModuleConfiguration syncModuleConfiguration;


  protected SyncEntityLocalLookUpKeys() { // for reflection

  }

  public SyncEntityLocalLookUpKeys(String entityType, String entityDatabaseId, String entityLocalLookUpKey, SyncModuleConfiguration syncModuleConfiguration) {
    this.entityType = entityType;
    this.entityDatabaseId = entityDatabaseId;
    this.entityLocalLookUpKey = entityLocalLookUpKey;
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

  public SyncModuleConfiguration getSyncModuleConfiguration() {
    return syncModuleConfiguration;
  }


  @Override
  public String toString() {
    return getEntityType() + ": " + getEntityLocalLookUpKey();
  }

}
