package net.dankito.sync.synchronization.modules;

import android.Manifest;
import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;

import net.dankito.sync.ContactSyncEntity;
import net.dankito.sync.EmailSyncEntity;
import net.dankito.sync.EmailType;
import net.dankito.sync.PhoneNumberSyncEntity;
import net.dankito.sync.PhoneNumberType;
import net.dankito.sync.SyncEntity;
import net.dankito.sync.SyncJobItem;
import net.dankito.sync.android.common.R;
import net.dankito.sync.localization.Localization;
import net.dankito.utils.IThreadPool;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;


public class AndroidContactsSyncModule extends AndroidSyncModuleBase implements ISyncModule {

  private static final Logger log = LoggerFactory.getLogger(AndroidContactsSyncModule.class);


  public AndroidContactsSyncModule(Context context, Localization localization, IThreadPool threadPool) {
    super(context, localization, threadPool);
  }


  @Override
  protected String getNameStringResourceKey() {
    return "sync.module.name.contacts";
  }

  @Override
  public int getDisplayPriority() {
    return DISPLAY_PRIORITY_HIGH;
  }

  public String getSyncEntityTypeItCanHandle() {
    return SyncModuleDefaultTypes.CONTACTS.getTypeName();
  }


  @Override
  protected Uri getContentUri() {
    return ContactsContract.RawContacts.CONTENT_URI;
  }

  @Override
  protected Uri getContentUriForContentObserver() {
    return ContactsContract.RawContacts.CONTENT_URI;
  }

  @Override
  public String getPermissionToReadEntities() {
    return Manifest.permission.READ_CONTACTS;
  }

  @Override
  public String getPermissionToWriteEntities() {
    return Manifest.permission.WRITE_CONTACTS;
  }

  @Override
  public int getPermissionRationaleResourceId() {
    return R.string.rational_for_accessing_contacts_permission;
  }


  @Override
  protected SyncEntity mapDatabaseEntryToSyncEntity(Cursor cursor) {
    boolean isDeleted = readBoolean(cursor, "deleted");
    if(isDeleted) {
      return null;
    }

    ContactSyncEntity entity = new ContactSyncEntity();

    Long rawContactId = readLong(cursor, ContactsContract.RawContacts._ID);

    entity.setLocalLookupKey("" + rawContactId);
    entity.setCreatedOnDevice(null); // TODO
    entity.setLastModifiedOnDevice(readDate(cursor, "version")); // TODO: don't know a better way to tell if raw contact has changed

    entity.setDisplayName(readString(cursor, ContactsContract.RawContacts.DISPLAY_NAME_PRIMARY));

    readRawContactDetails(entity, rawContactId);

    return entity;
  }

  protected void readRawContactDetails(ContactSyncEntity entity, Long rawContactId) {
    readPhoneNumbers(entity, rawContactId);

    readEmailAddresses(entity, rawContactId);

    readContactNameDetails(entity, rawContactId);

    entity.setNickname(readContactDetailString(rawContactId, ContactsContract.CommonDataKinds.Nickname.CONTENT_ITEM_TYPE,
        ContactsContract.CommonDataKinds.Nickname.NAME));

    entity.setNote(readContactDetailString(rawContactId, ContactsContract.CommonDataKinds.Note.CONTENT_ITEM_TYPE,
        ContactsContract.CommonDataKinds.Note.NOTE));

    entity.setWebsiteUrl(readContactDetailString(rawContactId, ContactsContract.CommonDataKinds.Website.CONTENT_ITEM_TYPE,
        ContactsContract.CommonDataKinds.Website.URL)); // theoretically there's also a ContactsContract.CommonDataKinds.Website.TYPE, but it cannot be edited in UI
  }

  protected void readPhoneNumbers(ContactSyncEntity entity, Long rawContactId) {
    Cursor phones = context.getContentResolver().query(
        ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
        ContactsContract.CommonDataKinds.Phone.RAW_CONTACT_ID + " = " + rawContactId,
        null, null);

    if(phones.moveToFirst()) {
      PhoneNumberSyncEntity phoneNumber = parsePhoneNumberSyncEntityFromCursor(phones);
      entity.addPhoneNumber(phoneNumber);

      while (phones.moveToNext()) {
        phoneNumber = parsePhoneNumberSyncEntityFromCursor(phones);
        entity.addPhoneNumber(phoneNumber);
      }
    }

    phones.close();
  }

  @NonNull
  protected PhoneNumberSyncEntity parsePhoneNumberSyncEntityFromCursor(Cursor phones) {
    PhoneNumberSyncEntity phoneNumber = new PhoneNumberSyncEntity();

    phoneNumber.setLocalLookupKey(readString(phones,ContactsContract.CommonDataKinds.Phone._ID));

    phoneNumber.setNumber(readString(phones, ContactsContract.CommonDataKinds.Phone.NUMBER));
    phoneNumber.setNormalizedNumber(readString(phones, ContactsContract.CommonDataKinds.Phone.NORMALIZED_NUMBER));
    phoneNumber.setType(parsePhoneNumberType(readInteger(phones, ContactsContract.CommonDataKinds.Phone.TYPE)));
    phoneNumber.setLabel(readString(phones, ContactsContract.CommonDataKinds.Phone.LABEL));

    return phoneNumber;
  }

  protected void readEmailAddresses(ContactSyncEntity entity, Long rawContactId) {
    Cursor emails = context.getContentResolver().query(
        ContactsContract.CommonDataKinds.Email.CONTENT_URI, null,
        ContactsContract.CommonDataKinds.Email.RAW_CONTACT_ID + " = " + rawContactId,
        null, null);

    if(emails.moveToFirst()) {
      EmailSyncEntity email = parseEmailSyncEntityFromCursor(emails);

      entity.setEmailAddress(email.getAddress());

      while (emails.moveToNext()) { // TODO: store additional email addresses
        // This would allow you get several email addresses
        email = parseEmailSyncEntityFromCursor(emails);
        if(email != null) { }
      }
    }

    emails.close();
  }

  @NonNull
  protected EmailSyncEntity parseEmailSyncEntityFromCursor(Cursor emails) {
    EmailSyncEntity email = new EmailSyncEntity();
    email.setAddress(readString(emails, ContactsContract.CommonDataKinds.Email.ADDRESS));
    email.setType(parseEmailType(readInteger(emails, ContactsContract.CommonDataKinds.Email.TYPE)));
    email.setLabel(readString(emails, ContactsContract.CommonDataKinds.Email.LABEL));
    return email;
  }

  protected void readContactNameDetails(ContactSyncEntity entity, Long rawContactId) {
    String where = ContactsContract.Data.RAW_CONTACT_ID + " = ? AND " + ContactsContract.Data.MIMETYPE + " = ?";
    String[] whereParameters = new String[] { "" + rawContactId, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE };

    Cursor structuredNames = context.getContentResolver().query(ContactsContract.Data.CONTENT_URI, null, where, whereParameters, null);

    if(structuredNames.moveToFirst()) {
      entity.setGivenName(readString(structuredNames, ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME));
      entity.setMiddleName(readString(structuredNames, ContactsContract.CommonDataKinds.StructuredName.MIDDLE_NAME));
      entity.setFamilyName(readString(structuredNames, ContactsContract.CommonDataKinds.StructuredName.FAMILY_NAME));

      entity.setPhoneticGivenName(readString(structuredNames, ContactsContract.CommonDataKinds.StructuredName.PHONETIC_GIVEN_NAME));
      entity.setPhoneticMiddleName(readString(structuredNames, ContactsContract.CommonDataKinds.StructuredName.PHONETIC_MIDDLE_NAME));
      entity.setPhoneticFamilyName(readString(structuredNames, ContactsContract.CommonDataKinds.StructuredName.PHONETIC_FAMILY_NAME));
    }

    structuredNames.close();
  }

  protected String readContactDetailString(Long rawContactId, String mimeType, String columnName) {
    String value = null;

    String[] columns = new String[] { columnName };
    String where = ContactsContract.Data.RAW_CONTACT_ID + " = ? AND " + ContactsContract.Data.MIMETYPE + " = ?";
    String[] whereParameters = new String[] { "" + rawContactId, mimeType };

    Cursor details = context.getContentResolver().query(ContactsContract.Data.CONTENT_URI, columns, where, whereParameters, null);

    if(details.moveToFirst()) {
     value = readString(details, columns[0]);
    }

    details.close();

    return value;
  }


  @Override
  protected boolean addEntityToLocalDatabase(SyncJobItem jobItem) {
    ContactSyncEntity entity = (ContactSyncEntity)jobItem.getEntity();

    ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();
    ops.add(ContentProviderOperation.newInsert(ContactsContract.RawContacts.CONTENT_URI)
        .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, null)
        .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, null)
        .build());

    ContentResolver resolver = context.getContentResolver();

    Long newRawContactId = null;

    try {
      ContentProviderResult[] results = resolver.applyBatch(ContactsContract.AUTHORITY, ops);

      if(results.length > 0 && results[0] != null) {
        String newRawContactIdString = results[0].uri.getLastPathSegment();
        newRawContactId = parseLocalLookupKeyToLong(newRawContactIdString);

        entity.setLocalLookupKey(newRawContactIdString);
      }
    } catch(Exception e) {
      log.error("Could not insert Contact into Database: " + entity, e);
    }

    if(entity.getLocalLookupKey() != null && newRawContactId != null) {
      return saveContactToDatabase(entity, newRawContactId);
    }

    return false;
  }

  protected boolean saveContactToDatabase(ContactSyncEntity entity, Long rawContactId) {
    boolean result;

    result = saveName(entity, rawContactId);

    result &= savePhoneNumbers(entity, rawContactId);

    result &= saveEmailAddresses(entity, rawContactId);

    result &= saveContactDetail(rawContactId, ContactsContract.CommonDataKinds.Nickname.CONTENT_ITEM_TYPE,
        ContactsContract.CommonDataKinds.Nickname.NAME, entity.getNickname());

    result &= saveContactDetail(rawContactId, ContactsContract.CommonDataKinds.Note.CONTENT_ITEM_TYPE,
        ContactsContract.CommonDataKinds.Note.NOTE, entity.getNote());

    result &= saveContactDetail(rawContactId, ContactsContract.CommonDataKinds.Website.CONTENT_ITEM_TYPE,
        ContactsContract.CommonDataKinds.Website.URL, entity.getWebsiteUrl());
    // theoretically there's also a ContactsContract.CommonDataKinds.Website.TYPE, but it cannot be edited in UI

    return result;
  }

  protected boolean saveName(ContactSyncEntity entity, Long rawContactId) {
    try {
      ContentValues values = mapEntityToNameContentValues(entity, rawContactId);

      Uri uri = context.getContentResolver().insert(ContactsContract.Data.CONTENT_URI, values);
      return wasInsertSuccessful(uri);
    } catch(Exception e) { log.error("Could not insert contact names into database for entity " + entity, e); }

    return false;
  }

  protected boolean savePhoneNumbers(ContactSyncEntity entity, Long rawContactId) {
    boolean result = entity.getPhoneNumbers().size() > 0;

    for(PhoneNumberSyncEntity phoneNumber : entity.getPhoneNumbers()) {
      result &= savePhoneNumber(phoneNumber, rawContactId);
    }

    return result;
  }

  protected boolean savePhoneNumber(PhoneNumberSyncEntity phoneNumber, Long rawContactId) {
    try {
      ContentValues values = mapEntityToPhoneNumberContentValues(phoneNumber, rawContactId);

      Uri uri = context.getContentResolver().insert(ContactsContract.Data.CONTENT_URI, values);

      long newPhoneNumberId = ContentUris.parseId(uri);
      phoneNumber.setLocalLookupKey("" + newPhoneNumberId);

      // TODO: update normalized phone number
      return wasInsertSuccessful(uri);
    } catch(Exception e) { log.error("Could not insert phone number into database for entity " + phoneNumber, e); }

    return false;
  }

  protected boolean saveEmailAddresses(ContactSyncEntity entity, Long rawContactId) {
    // TODO: save all email addresses

    try {
      ContentValues values = mapEntityToEmailAddressContentValues(entity, rawContactId);

      Uri uri = context.getContentResolver().insert(ContactsContract.Data.CONTENT_URI, values);
      return wasInsertSuccessful(uri);
    } catch(Exception e) { log.error("Could not insert email address into database for entity " + entity, e); }

    return false;
  }

  protected boolean saveContactDetail(Long rawContactId, String mimeType, String valueColumnName, String value) {
    try {
      ContentValues values = mapEntityToContactDetailContentValues(rawContactId, mimeType, valueColumnName, value);

      Uri uri = context.getContentResolver().insert(ContactsContract.Data.CONTENT_URI, values);
      return wasInsertSuccessful(uri);
    } catch(Exception e) { log.error("Could not insert value '" + value + "' of mime type '" + mimeType + "' for raw contact " + rawContactId, e); }

    return false;
  }


  @Override
  protected boolean updateEntityInLocalDatabase(SyncJobItem jobItem) {
    ContactSyncEntity entity = (ContactSyncEntity)jobItem.getEntity();

    Long rawContactId = parseLocalLookupKeyToLong(entity);
    if(rawContactId == null) {
      return false;
    }


    updateName(entity, rawContactId);

    updatePhoneNumbers(entity, rawContactId);

    updateEmailAddresses(entity, rawContactId);

    updateContactDetail(rawContactId, ContactsContract.CommonDataKinds.Nickname.CONTENT_ITEM_TYPE,
        ContactsContract.CommonDataKinds.Nickname.NAME, entity.getNickname());

    updateContactDetail(rawContactId, ContactsContract.CommonDataKinds.Note.CONTENT_ITEM_TYPE,
        ContactsContract.CommonDataKinds.Note.NOTE, entity.getNote());

    updateContactDetail(rawContactId, ContactsContract.CommonDataKinds.Website.CONTENT_ITEM_TYPE,
        ContactsContract.CommonDataKinds.Website.URL, entity.getWebsiteUrl());
    // theoretically there's also a ContactsContract.CommonDataKinds.Website.TYPE, but it cannot be edited in UI

    return true; // checking all updateXyz() methods return values is wrong as if there was nothing to update, int result = 0 and therefore false will be returned
  }

  protected boolean updateName(ContactSyncEntity entity, Long rawContactId) {
    try {
      ContentValues values = mapEntityToNameContentValues(entity, rawContactId);

      int result = context.getContentResolver().update(ContactsContract.Data.CONTENT_URI, values,
          ContactsContract.Data.RAW_CONTACT_ID + " = ? AND " + ContactsContract.Data.MIMETYPE + " = ?",
          new String[] { "" + rawContactId, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE });

      return result > 0; // if there was nothing to update, result is 0
    } catch(Exception e) { log.error("Could not update contact names in database for entity " + entity, e); }

    return false;
  }

  protected boolean updatePhoneNumbers(ContactSyncEntity entity, Long rawContactId) {
    boolean result = entity.getPhoneNumbers().size() > 0;

    for(PhoneNumberSyncEntity phoneNumber : entity.getPhoneNumbers()) {
      result &= updatePhoneNumber(phoneNumber, rawContactId);
    }

    return result;
  }

  protected boolean updatePhoneNumber(PhoneNumberSyncEntity phoneNumber, Long rawContactId) {
    try {
      ContentValues values = mapEntityToPhoneNumberContentValues(phoneNumber, rawContactId);

      int result = context.getContentResolver().update(ContactsContract.Data.CONTENT_URI, values,
          ContactsContract.CommonDataKinds.Phone._ID + " = ? AND " + ContactsContract.Data.MIMETYPE + " = ?",
          new String[] { phoneNumber.getLocalLookupKey(), ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE });

      // TODO: update normalized phone number
      return result > 0;
    } catch(Exception e) { log.error("Could not update phone number in database for entity " + phoneNumber, e); }

    return false;
  }

  protected boolean updateEmailAddresses(ContactSyncEntity entity, Long rawContactId) {
    // TODO: save all email addresses

    try {
      ContentValues values = mapEntityToEmailAddressContentValues(entity, rawContactId);

      int result = context.getContentResolver().update(ContactsContract.Data.CONTENT_URI, values,
          ContactsContract.Data.RAW_CONTACT_ID + " = ? AND " + ContactsContract.Data.MIMETYPE + " = ?",
          new String[] { "" + rawContactId, ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE });

      return result > 0;
    } catch(Exception e) { log.error("Could not update email address in database for entity " + entity, e); }

    return false;
  }

  protected boolean updateContactDetail(Long rawContactId, String mimeType, String valueColumnName, String value) {
    try {
      ContentValues values = mapEntityToContactDetailContentValues(rawContactId, mimeType, valueColumnName, value);

      int result = context.getContentResolver().update(ContactsContract.Data.CONTENT_URI, values,
          ContactsContract.Data.RAW_CONTACT_ID + " = ? AND " + ContactsContract.Data.MIMETYPE + " = ?",
          new String[] { "" + rawContactId, mimeType });

      return result > 0; // if there was nothing to update, result is 0
    } catch(Exception e) { log.error("Could not update value '" + value + "' of mime type '" + mimeType + "' for raw contact " + rawContactId, e); }

    return false;
  }


  @Override
  public boolean deleteSyncEntityProperty(SyncEntity entity, SyncEntity property) {
    if(property instanceof PhoneNumberSyncEntity) {
      return deletePhoneNumber((ContactSyncEntity)entity, (PhoneNumberSyncEntity)property);
    }
    else {
      return super.deleteSyncEntityProperty(entity, property);
    }
  }

  protected ContentValues mapEntityToNameContentValues(ContactSyncEntity entity, Long rawContactId) {
    ContentValues values = new ContentValues();

    values.put(ContactsContract.CommonDataKinds.Phone.RAW_CONTACT_ID, rawContactId);
    values.put(ContactsContract.CommonDataKinds.Phone.MIMETYPE, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE);

    values.put(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, entity.getDisplayName());

    values.put(ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME, entity.getGivenName());
    values.put(ContactsContract.CommonDataKinds.StructuredName.MIDDLE_NAME, entity.getMiddleName());
    values.put(ContactsContract.CommonDataKinds.StructuredName.FAMILY_NAME, entity.getFamilyName());

    values.put(ContactsContract.CommonDataKinds.StructuredName.PHONETIC_GIVEN_NAME, entity.getPhoneticGivenName());
    values.put(ContactsContract.CommonDataKinds.StructuredName.PHONETIC_MIDDLE_NAME, entity.getPhoneticMiddleName());
    values.put(ContactsContract.CommonDataKinds.StructuredName.PHONETIC_FAMILY_NAME, entity.getPhoneticFamilyName());

    return values;
  }

  @NonNull
  protected ContentValues mapEntityToPhoneNumberContentValues(PhoneNumberSyncEntity phoneNumber, Long rawContactId) {
    ContentValues values = new ContentValues();

    values.put(ContactsContract.CommonDataKinds.Phone._ID, phoneNumber.getLocalLookupKey());
    values.put(ContactsContract.CommonDataKinds.Phone.RAW_CONTACT_ID, rawContactId);
    values.put(ContactsContract.CommonDataKinds.Phone.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE);

    values.put(ContactsContract.CommonDataKinds.Phone.NUMBER, phoneNumber.getNumber());
    values.put(ContactsContract.CommonDataKinds.Phone.TYPE, mapPhoneNumberTypeToAndroidPhoneNumberType(phoneNumber.getType()));
    values.put(ContactsContract.CommonDataKinds.Phone.LABEL, phoneNumber.getLabel());

    return values;
  }

  @NonNull
  protected ContentValues mapEntityToEmailAddressContentValues(ContactSyncEntity entity, Long rawContactId) {
    ContentValues values = new ContentValues();

    values.put(ContactsContract.CommonDataKinds.Email.RAW_CONTACT_ID, rawContactId);
    values.put(ContactsContract.CommonDataKinds.Email.MIMETYPE, ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE);

    values.put(ContactsContract.CommonDataKinds.Email.ADDRESS, entity.getEmailAddress());
    values.put(ContactsContract.CommonDataKinds.Email.TYPE, ContactsContract.CommonDataKinds.Email.TYPE_HOME);
    // TODO: add custom type label
//    values.put(ContactsContract.CommonDataKinds.Email.LABEL, entity.);
    return values;
  }

  @NonNull
  protected ContentValues mapEntityToContactDetailContentValues(Long rawContactId, String mimeType, String valueColumnName, String value) {
    return mapEntityToContactDetailContentValues(ContactsContract.Data.RAW_CONTACT_ID, rawContactId, ContactsContract.Data.MIMETYPE, mimeType, valueColumnName, value);
  }

  @NonNull
  protected ContentValues mapEntityToContactDetailContentValues(String rawContactIdColumnName, Long rawContactId, String mimeTypeColumnName, String mimeType, String valueColumnName, String value) {
    ContentValues values = new ContentValues();

    values.put(rawContactIdColumnName, rawContactId);
    values.put(mimeTypeColumnName, mimeType);

    values.put(valueColumnName, value);

    return values;
  }


  protected EmailType parseEmailType(int emailTypeOrdinal) {
    switch(emailTypeOrdinal) {
      case ContactsContract.CommonDataKinds.Email.TYPE_HOME:
        return EmailType.HOME;
      case ContactsContract.CommonDataKinds.Email.TYPE_WORK:
        return EmailType.WORK;
      case ContactsContract.CommonDataKinds.Email.TYPE_MOBILE:
        return EmailType.MOBILE;
      default:
        return EmailType.OTHER;
    }
  }

  protected int mapEmailTypeToAndroidEmailType(EmailType type) {
    switch(type) {
      case HOME:
        return ContactsContract.CommonDataKinds.Email.TYPE_HOME;
      case MOBILE:
        return ContactsContract.CommonDataKinds.Email.TYPE_MOBILE;
      case WORK:
        return ContactsContract.CommonDataKinds.Email.TYPE_WORK;
      default:
        return ContactsContract.CommonDataKinds.Email.TYPE_OTHER;
    }
  }

  protected PhoneNumberType parsePhoneNumberType(int phoneNumberTypeOrdinal) {
    switch(phoneNumberTypeOrdinal) {
      case ContactsContract.CommonDataKinds.Phone.TYPE_HOME:
        return PhoneNumberType.HOME;
      case ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE:
        return PhoneNumberType.MOBILE;
      case ContactsContract.CommonDataKinds.Phone.TYPE_WORK:
        return PhoneNumberType.WORK;
      default:
        return PhoneNumberType.OTHER;
    }
  }

  protected int mapPhoneNumberTypeToAndroidPhoneNumberType(PhoneNumberType type) {
    switch(type) {
      case HOME:
        return ContactsContract.CommonDataKinds.Phone.TYPE_HOME;
      case MOBILE:
        return ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE;
      case WORK:
        return ContactsContract.CommonDataKinds.Phone.TYPE_WORK;
      default:
        return ContactsContract.CommonDataKinds.Phone.TYPE_OTHER;
    }
  }

  protected boolean deletePhoneNumber(ContactSyncEntity contact, PhoneNumberSyncEntity phoneNumber) {
    ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();

    String selectPhone = ContactsContract.Data.RAW_CONTACT_ID + " = ? AND " +
        ContactsContract.Data.MIMETYPE + " = ? AND " +
        ContactsContract.CommonDataKinds.Phone._ID + " = ?";
    String[] phoneArgs = new String[] { contact.getLocalLookupKey(),
        ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE,
        phoneNumber.getLocalLookupKey()};

    ops.add(ContentProviderOperation.newDelete(ContactsContract.Data.CONTENT_URI)
        .withSelection(selectPhone, phoneArgs).build());

    try {
      ContentProviderResult[] results = context.getContentResolver().applyBatch(ContactsContract.AUTHORITY, ops);
      return results != null && results.length > 0 && wasInsertSuccessful(results[0].uri);
    } catch (Exception e) {
      log.error("Could not deleted phone number " + phoneNumber + " of " + contact);
    }

    return false;
  }


  protected void updateLastModifiedDate(SyncJobItem jobItem) {
    SyncEntity syncEntity = jobItem.getEntity();

    Cursor cursor = context.getContentResolver().query(
        getContentUri(),
        new String[] { "version" },
        ContactsContract.RawContacts._ID + " = ? ",
        new String[] { syncEntity.getLocalLookupKey() },
        null        // Ordering
    );

    if(cursor.moveToFirst()) {
      syncEntity.setLastModifiedOnDevice(readDate(cursor, "version"));
    }

    cursor.close();
  }

}
