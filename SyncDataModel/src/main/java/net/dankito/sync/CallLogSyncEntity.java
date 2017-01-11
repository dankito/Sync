package net.dankito.sync;

import net.dankito.sync.config.DatabaseTableConfig;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;


@Entity(name = DatabaseTableConfig.CALL_LOG_SYNC_ENTITY_TABLE_NAME)
@DiscriminatorValue(value = DatabaseTableConfig.CALL_LOG_SYNC_ENTITY_DISCRIMINATOR_VALUE)
public class CallLogSyncEntity extends SyncEntity {

  @Column(name = DatabaseTableConfig.CALL_LOG_SYNC_ENTITY_NUMBER_COLUMN_NAME)
  protected String number;

  @Column(name = DatabaseTableConfig.CALL_LOG_SYNC_ENTITY_NORMALIZED_NUMBER_COLUMN_NAME)
  protected String normalizedNumber;

  @Column(name = DatabaseTableConfig.CALL_LOG_SYNC_ENTITY_ASSOCIATED_CONTACT_NAME_COLUMN_NAME)
  protected String associatedContactName;

  @Column(name = DatabaseTableConfig.CALL_LOG_SYNC_ENTITY_ASSOCIATED_CONTACT_LOOK_UP_KEY_COLUMN_NAME)
  protected String associatedContactLookUpKey;

  @Temporal(value = TemporalType.TIMESTAMP)
  @Column(name = DatabaseTableConfig.CALL_LOG_SYNC_ENTITY_DATE_COLUMN_NAME)
  protected Date date;

  @Column(name = DatabaseTableConfig.CALL_LOG_SYNC_ENTITY_DURATION_IN_SECONDS_COLUMN_NAME)
  protected int durationInSeconds;

  @Enumerated(value = EnumType.ORDINAL)
  @Column(name = DatabaseTableConfig.CALL_LOG_SYNC_ENTITY_TYPE_COLUMN_NAME)
  protected CallType type;


  public CallLogSyncEntity() {

  }


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

  public String getAssociatedContactName() {
    return associatedContactName;
  }

  public void setAssociatedContactName(String associatedContactName) {
    this.associatedContactName = associatedContactName;
  }

  public String getAssociatedContactLookUpKey() {
    return associatedContactLookUpKey;
  }

  public void setAssociatedContactLookUpKey(String associatedContactLookUpKey) {
    this.associatedContactLookUpKey = associatedContactLookUpKey;
  }

  public Date getDate() {
    return date;
  }

  public void setDate(Date date) {
    this.date = date;
  }

  public int getDurationInSeconds() {
    return durationInSeconds;
  }

  public void setDurationInSeconds(int durationInSeconds) {
    this.durationInSeconds = durationInSeconds;
  }

  public CallType getType() {
    return type;
  }

  public void setType(CallType type) {
    this.type = type;
  }
}
