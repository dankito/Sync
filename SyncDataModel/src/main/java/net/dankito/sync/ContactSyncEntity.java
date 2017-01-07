package net.dankito.sync;


import net.dankito.sync.config.DatabaseTableConfig;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;


@Entity(name = DatabaseTableConfig.CONTACT_SYNC_ENTITY_TABLE_NAME)
@DiscriminatorValue(value = DatabaseTableConfig.CONTACT_SYNC_ENTITY_DISCRIMINATOR_VALUE)
public class ContactSyncEntity extends SyncEntity {

  @Column(name = DatabaseTableConfig.CONTACT_SYNC_ENTITY_DISPLAY_NAME_COLUMN_NAME)
  protected String displayName;

  @Column(name = DatabaseTableConfig.CONTACT_SYNC_ENTITY_NICKNAME_COLUMN_NAME)
  protected String nickname;

  @Column(name = DatabaseTableConfig.CONTACT_SYNC_ENTITY_GIVEN_NAME_COLUMN_NAME)
  protected String givenName;

  @Column(name = DatabaseTableConfig.CONTACT_SYNC_ENTITY_MIDDLE_NAME_COLUMN_NAME)
  protected String middleName;

  @Column(name = DatabaseTableConfig.CONTACT_SYNC_ENTITY_FAMILY_NAME_COLUMN_NAME)
  protected String familyName;

  @Column(name = DatabaseTableConfig.CONTACT_SYNC_ENTITY_PHONETIC_GIVEN_NAME_COLUMN_NAME)
  protected String phoneticGivenName;

  @Column(name = DatabaseTableConfig.CONTACT_SYNC_ENTITY_PHONETIC_MIDDLE_NAME_COLUMN_NAME)
  protected String phoneticMiddleName;

  @Column(name = DatabaseTableConfig.CONTACT_SYNC_ENTITY_PHONETIC_FAMILY_NAME_COLUMN_NAME)
  protected String phoneticFamilyName;

  @Column(name = DatabaseTableConfig.CONTACT_SYNC_ENTITY_PHONE_NUMBER_COLUMN_NAME)
  protected String phoneNumber;

  @Column(name = DatabaseTableConfig.CONTACT_SYNC_ENTITY_EMAIL_ADDRESS_COLUMN_NAME)
  protected String emailAddress;

  @Column(name = DatabaseTableConfig.CONTACT_SYNC_ENTITY_WEBSITE_URL_COLUMN_NAME)
  protected String websiteUrl;

  @Column(name = DatabaseTableConfig.CONTACT_SYNC_ENTITY_NOTE_COLUMN_NAME)
  protected String note;


  protected ContactSyncEntity() { // for reflection

  }

  public ContactSyncEntity(SyncModuleConfiguration syncModuleConfiguration) {
    super(syncModuleConfiguration);
  }


  public String getDisplayName() {
    return displayName;
  }

  public void setDisplayName(String displayName) {
    this.displayName = displayName;
  }

  public String getNickname() {
    return nickname;
  }

  public void setNickname(String nickname) {
    this.nickname = nickname;
  }

  public String getGivenName() {
    return givenName;
  }

  public void setGivenName(String givenName) {
    this.givenName = givenName;
  }

  public String getMiddleName() {
    return middleName;
  }

  public void setMiddleName(String middleName) {
    this.middleName = middleName;
  }

  public String getFamilyName() {
    return familyName;
  }

  public void setFamilyName(String familyName) {
    this.familyName = familyName;
  }

  public String getPhoneticGivenName() {
    return phoneticGivenName;
  }

  public void setPhoneticGivenName(String phoneticGivenName) {
    this.phoneticGivenName = phoneticGivenName;
  }

  public String getPhoneticMiddleName() {
    return phoneticMiddleName;
  }

  public void setPhoneticMiddleName(String phoneticMiddleName) {
    this.phoneticMiddleName = phoneticMiddleName;
  }

  public String getPhoneticFamilyName() {
    return phoneticFamilyName;
  }

  public void setPhoneticFamilyName(String phoneticFamilyName) {
    this.phoneticFamilyName = phoneticFamilyName;
  }

  public String getPhoneNumber() {
    return phoneNumber;
  }

  public void setPhoneNumber(String phoneNumber) {
    this.phoneNumber = phoneNumber;
  }

  public String getEmailAddress() {
    return emailAddress;
  }

  public void setEmailAddress(String emailAddress) {
    this.emailAddress = emailAddress;
  }

  public String getWebsiteUrl() {
    return websiteUrl;
  }

  public void setWebsiteUrl(String websiteUrl) {
    this.websiteUrl = websiteUrl;
  }

  public String getNote() {
    return note;
  }

  public void setNote(String note) {
    this.note = note;
  }


  @Override
  public String toString() {
    return getDisplayName();
  }

}
