package net.dankito.sync;


import net.dankito.sync.config.DatabaseTableConfig;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;


@Entity(name = DatabaseTableConfig.EMAIL_SYNC_ENTITY_TABLE_NAME)
public class EmailSyncEntity extends SyncEntity {

  @Column(name = DatabaseTableConfig.EMAIL_SYNC_ENTITY_ADDRESS_COLUMN_NAME)
  protected String address;

  @Enumerated(EnumType.STRING)
  @Column(name = DatabaseTableConfig.EMAIL_SYNC_ENTITY_TYPE_COLUMN_NAME)
  protected EmailType type;

  /**
   * The label is used for custom EmailTypes
   */
  @Column(name = DatabaseTableConfig.EMAIL_SYNC_ENTITY_LABEL_COLUMN_NAME)
  protected String label;


  public EmailSyncEntity() {

  }

  public EmailSyncEntity(String address, EmailType type) {
    this.address = address;
    this.type = type;
  }

  public EmailSyncEntity(String address, String label) {
    this.address = address;
    this.label = label;
  }


  public String getAddress() {
    return address;
  }

  public void setAddress(String address) {
    this.address = address;
  }

  public EmailType getType() {
    return type;
  }

  public void setType(EmailType type) {
    this.type = type;
  }

  public String getLabel() {
    return label;
  }

  public void setLabel(String label) {
    this.label = label;
  }


  @Override
  public String toString() {
    return getAddress();
  }

}
