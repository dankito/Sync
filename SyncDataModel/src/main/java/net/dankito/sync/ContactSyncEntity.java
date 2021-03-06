package net.dankito.sync;


import net.dankito.sync.config.DatabaseTableConfig;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.Transient;


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

  @OneToMany
  protected List<PhoneNumberSyncEntity> phoneNumbers = new ArrayList<>();

  @OneToMany
  protected List<EmailSyncEntity> emailAddresses = new ArrayList<>();

  @Column(name = DatabaseTableConfig.CONTACT_SYNC_ENTITY_WEBSITE_URL_COLUMN_NAME)
  protected String websiteUrl;

  @Column(name = DatabaseTableConfig.CONTACT_SYNC_ENTITY_NOTE_COLUMN_NAME)
  protected String note;


  public ContactSyncEntity() {

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

  @Transient
  public boolean hasPhoneNumbers() {
    return getPhoneNumbers().size() > 0;
  }

  public List<PhoneNumberSyncEntity> getPhoneNumbers() {
    return phoneNumbers;
  }

  public boolean addPhoneNumber(PhoneNumberSyncEntity phoneNumber) {
    return phoneNumbers.add(phoneNumber);
  }

  public boolean removePhoneNumber(PhoneNumberSyncEntity phoneNumber) {
    return phoneNumbers.remove(phoneNumber);
  }

  @Transient
  public boolean hasEmailAddresses() {
    return getEmailAddresses().size() > 0;
  }

  public List<EmailSyncEntity> getEmailAddresses() {
    return emailAddresses;
  }

  public boolean addEmailAddress(EmailSyncEntity email) {
    return emailAddresses.add(email);
  }

  public boolean removeEmailAddress(EmailSyncEntity email) {
    return emailAddresses.remove(email);
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
    return "Contact: " + getDisplayName();
  }

}
