package net.dankito.sync;


import net.dankito.sync.config.DatabaseTableConfig;

import javax.persistence.Column;
import javax.persistence.Entity;


@Entity(name = DatabaseTableConfig.PHONE_NUMBER_SYNC_ENTITY_TABLE_NAME)
public class PhoneNumberSyncEntity extends SyncEntity {

  @Column(name = DatabaseTableConfig.PHONE_NUMBER_SYNC_ENTITY_NUMBER_COLUMN_NAME)
  protected String number;

  @Column(name = DatabaseTableConfig.PHONE_NUMBER_SYNC_ENTITY_NORMALIZED_NUMBER_COLUMN_NAME)
  protected String normalizedNumber;

  @Column(name = DatabaseTableConfig.PHONE_NUMBER_SYNC_ENTITY_TYPE_COLUMN_NAME)
  protected PhoneNumberType type;

  @Column(name = DatabaseTableConfig.PHONE_NUMBER_SYNC_ENTITY_LABEL_COLUMN_NAME)
  protected String label;


  public String getNumber() {
    return number;
  }

  public void setNumber(String number) {
    this.number = number;
  }

  public String getNormalizedNumber() {
    return normalizedNumber;
  }

  public void setNormalizedNumber(String normalizedNumber) {
    this.normalizedNumber = normalizedNumber;
  }

  public PhoneNumberType getType() {
    return type;
  }

  public void setType(PhoneNumberType type) {
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
    return getNumber();
  }

}
