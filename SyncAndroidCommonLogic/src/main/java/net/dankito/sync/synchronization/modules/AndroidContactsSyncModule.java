package net.dankito.sync.synchronization.modules;

import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import net.dankito.sync.ContactSyncEntity;
import net.dankito.sync.SyncEntity;
import net.dankito.sync.SyncJobItem;
import net.dankito.utils.IThreadPool;
import net.dankito.utils.StringUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by ganymed on 05/01/17.
 */

public class AndroidContactsSyncModule extends AndroidSyncModuleBase implements ISyncModule {

  private static final Logger log = LoggerFactory.getLogger(AndroidContactsSyncModule.class);


  public AndroidContactsSyncModule(Context context, IThreadPool threadPool) {
    super(context, threadPool);
  }


  public String[] getSyncEntityTypesItCanHandle() {
    return new String[] { SyncModuleDefaultTypes.Contacts.getTypeName() };
  }


  @Override
  protected Uri getContentUri() {
    return ContactsContract.Contacts.CONTENT_URI;
  }

  @Override
  protected Uri getContentUriForContentObserver() {
    return ContactsContract.Contacts.CONTENT_URI;
  }

  @Override
  protected SyncEntity mapDatabaseEntryToSyncEntity(Cursor cursor) {
    ContactSyncEntity entity = new ContactSyncEntity();

    entity.setLookUpKeyOnSourceDevice(readString(cursor, ContactsContract.Contacts.LOOKUP_KEY));
    entity.setCreatedOnDevice(null); // TODO
    entity.setLastModifiedOnDevice(readDate(cursor, ContactsContract.Contacts.CONTACT_LAST_UPDATED_TIMESTAMP));

    entity.setDisplayName(readString(cursor, ContactsContract.Contacts.DISPLAY_NAME));

    long contactId = readLong(cursor, ContactsContract.Contacts._ID);
    Set<Long> rawContactIds = getRawContactIdsForContact(contactId);

    boolean hasPhoneNumber = readBoolean(cursor, ContactsContract.Contacts.HAS_PHONE_NUMBER);

    readRawContactsForContact(entity, rawContactIds, hasPhoneNumber);

    return entity;
  }

  protected Set<Long> getRawContactIdsForContact(long contactId) {
    HashSet<Long> ids = new HashSet<Long>();

    Cursor cursor = context.getContentResolver().query(ContactsContract.RawContacts.CONTENT_URI,
        new String[]{ ContactsContract.RawContacts._ID },
        ContactsContract.RawContacts.CONTACT_ID + "=?",
        new String[]{String.valueOf(contactId)}, null);

    if(cursor != null && cursor.moveToFirst()) {
      do {
        ids.add(cursor.getLong(0));
      } while (cursor.moveToNext());

      cursor.close();
    }

    return ids;
  }

  protected void readRawContactsForContact(ContactSyncEntity entity, Set<Long> rawContactIds, boolean hasPhoneNumber) {
    for(Long rawContactId : rawContactIds) {
      if(hasPhoneNumber) {
        readPhoneNumbers(entity, rawContactId);
      }

      readEmailAddresses(entity, rawContactId);

      readContactNameDetails(entity, rawContactId);

      entity.setNickname(readContactDetailString(rawContactId, ContactsContract.CommonDataKinds.Nickname.CONTENT_ITEM_TYPE,
          ContactsContract.CommonDataKinds.Nickname.NAME));

      entity.setNote(readContactDetailString(rawContactId, ContactsContract.CommonDataKinds.Note.CONTENT_ITEM_TYPE,
          ContactsContract.CommonDataKinds.Note.NOTE));

      entity.setWebsiteUrl(readContactDetailString(rawContactId, ContactsContract.CommonDataKinds.Website.CONTENT_ITEM_TYPE,
          ContactsContract.CommonDataKinds.Website.URL)); // theoretically there's also a ContactsContract.CommonDataKinds.Website.TYPE, but it cannot be edited in UI
    }
  }

  protected void readPhoneNumbers(ContactSyncEntity entity, Long rawContactId) {
    Cursor phones = context.getContentResolver().query(
        ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
        ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = " + rawContactId,
        null, null);

    if(phones.moveToFirst()) {
      entity.setPhoneNumber(readString(phones, ContactsContract.CommonDataKinds.Phone.NUMBER));
      int phoneType = readInteger(phones, ContactsContract.CommonDataKinds.Phone.TYPE); // ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE etc.

      while (phones.moveToNext()) { // TODO: store additional phone numbers
        String phoneNumber = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
        if(phoneNumber != null) { }
      }
    }

    phones.close();
  }

  protected void readEmailAddresses(ContactSyncEntity entity, Long rawContactId) {
    Cursor emails = context.getContentResolver().query(
        ContactsContract.CommonDataKinds.Email.CONTENT_URI, null,
        ContactsContract.CommonDataKinds.Email.CONTACT_ID + " = " + rawContactId,
        null, null);

    if(emails.moveToFirst()) {
      entity.setEmailAddress(readString(emails, ContactsContract.CommonDataKinds.Email.DATA));
      int emailType = readInteger(emails, ContactsContract.CommonDataKinds.Email.TYPE); // ContactsContract.CommonDataKinds.Email.TYPE_HOME etc.

      while (emails.moveToNext()) { // TODO: store additional email addresses
        // This would allow you get several email addresses
        String emailAddress = emails.getString(
            emails.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA));
        if(emailAddress != null) { }
      }
    }

    emails.close();
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
        newRawContactId = Long.parseLong(newRawContactIdString);

        getLookupKeyForNewContact(entity, resolver, newRawContactIdString);
      }
    } catch(Exception e) {
      log.error("Could not insert Contact into Database: " + entity, e);
    }

    if(entity.getLookUpKeyOnSourceDevice() != null && newRawContactId != null) {
      return saveContactToDatabase(entity, newRawContactId);
    }

    return false;
  }

  protected void getLookupKeyForNewContact(ContactSyncEntity entity, ContentResolver resolver, String newRawContactIdString) {
    Cursor rawContactCursor = resolver.query(ContactsContract.RawContacts.CONTENT_URI, new String[] { ContactsContract.RawContacts.CONTACT_ID },
        ContactsContract.RawContacts._ID + " = ? ", new String[] { newRawContactIdString }, null);

    if(rawContactCursor.moveToFirst()) {
      String contactId = readString(rawContactCursor, ContactsContract.RawContacts.CONTACT_ID);

      String contactLookupKey = getLookupKeyForContact(contactId);
      if(contactLookupKey != null) {
        entity.setLookUpKeyOnSourceDevice(contactLookupKey);
      }
    }
  }

  @Nullable
  protected String getLookupKeyForContact(String contactId) {
    Cursor lookupKeyCursor = context.getContentResolver().query(
        ContactsContract.Contacts.CONTENT_URI, new String[] { ContactsContract.Contacts.LOOKUP_KEY },
        ContactsContract.Contacts._ID + " = ? ", new String[] { contactId }, null);

    if (lookupKeyCursor.moveToFirst()) {
      return readString(lookupKeyCursor, ContactsContract.Contacts.LOOKUP_KEY);
    }

    return null;
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
    // TODO: save all phone numbers

    try {
      ContentValues values = mapEntityToPhoneNumberContentValues(entity, rawContactId);

      Uri uri = context.getContentResolver().insert(ContactsContract.Data.CONTENT_URI, values);
      return wasInsertSuccessful(uri);
    } catch(Exception e) { log.error("Could not insert phone number into database for entity " + entity, e); }

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

    Long contactId = getContactIdForContact(entity);
    if(contactId == null) {
      return false;
    }

    Set<Long> rawContactIds = getRawContactIdsForContact(contactId);
    boolean result = rawContactIds.size() > 0;

    if(rawContactIds.size() > 0) {
      // TODO: as i don't know from which RawContact the original data originated, i simply choose the first one and update that one. Not ideal though
      Long rawContactId = new ArrayList<>(rawContactIds).get(0);

      result &= updateName(entity, rawContactId);

      result &= updatePhoneNumbers(entity, rawContactId);

      result &= updateEmailAddresses(entity, rawContactId);

      result &= updateContactDetail(rawContactId, ContactsContract.CommonDataKinds.Nickname.CONTENT_ITEM_TYPE,
          ContactsContract.CommonDataKinds.Nickname.NAME, entity.getNickname());

      result &= updateContactDetail(rawContactId, ContactsContract.CommonDataKinds.Note.CONTENT_ITEM_TYPE,
          ContactsContract.CommonDataKinds.Note.NOTE, entity.getNote());

      result &= updateContactDetail(rawContactId, ContactsContract.CommonDataKinds.Website.CONTENT_ITEM_TYPE,
          ContactsContract.CommonDataKinds.Website.URL, entity.getWebsiteUrl());
      // theoretically there's also a ContactsContract.CommonDataKinds.Website.TYPE, but it cannot be edited in UI
    }


    Cursor cursor = context.getContentResolver().query(ContactsContract.Contacts.CONTENT_URI,
        new String[]{ ContactsContract.Contacts.LOOKUP_KEY },
        ContactsContract.Contacts._ID + "=?",
        new String[]{String.valueOf(contactId)}, null);

    if(cursor != null && cursor.moveToFirst()) {
      entity.setLookUpKeyOnSourceDevice(readString(cursor, ContactsContract.Contacts.LOOKUP_KEY));

      cursor.close();
    }

    return result;
  }

  protected Long getContactIdForContact(ContactSyncEntity contact) {
    try {
      return Long.parseLong(getContactIdStringForContact(contact));
    } catch(Exception e) { log.error("Could not get ContactId for Contact " + contact); }

    return null;
  }

  protected String getContactIdStringForContact(ContactSyncEntity contact) {
    String lookupKey = contact.getLookUpKeyOnSourceDevice();
    if(lookupKey.endsWith("-")) {
      lookupKey = lookupKey.substring(0, lookupKey.length() - 1);
    }

    Cursor contactIdCursor = context.getContentResolver().query(
        ContactsContract.Contacts.CONTENT_URI,
        null,
        ContactsContract.Contacts.LOOKUP_KEY + " LIKE ?", new String[] { lookupKey + "%" }, null
    );

    if(contactIdCursor.moveToFirst()) {
      String contactId = readString(contactIdCursor, ContactsContract.Contacts._ID);
      contactIdCursor.close();

      return contactId;
    }

    return null;
  }

  protected boolean updateName(ContactSyncEntity entity, Long rawContactId) {
    try {
      ContentValues values = mapEntityToNameContentValues(entity, rawContactId);

      int result = context.getContentResolver().update(ContactsContract.Data.CONTENT_URI, values,
          ContactsContract.Data.RAW_CONTACT_ID + " = ? AND " + ContactsContract.Data.MIMETYPE + " = ?",
          new String[] { "" + rawContactId, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE });

      return result > 0;
    } catch(Exception e) { log.error("Could not update contact names in database for entity " + entity, e); }

    return false;
  }

  protected boolean updatePhoneNumbers(ContactSyncEntity entity, Long rawContactId) {
    // TODO: save all phone numbers

    try {
      ContentValues values = mapEntityToPhoneNumberContentValues(entity, rawContactId);

      int result = context.getContentResolver().update(ContactsContract.Data.CONTENT_URI, values,
          ContactsContract.Data.RAW_CONTACT_ID + " = ? AND " + ContactsContract.Data.MIMETYPE + " = ?",
          new String[] { "" + rawContactId, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE });

      return result > 0;
    } catch(Exception e) { log.error("Could not update phone number in database for entity " + entity, e); }

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

      return result > 0;
    } catch(Exception e) { log.error("Could not update value '" + value + "' of mime type '" + mimeType + "' for raw contact " + rawContactId, e); }

    return false;
  }


  @Override
  protected boolean deleteEntityFromLocalDatabase(SyncJobItem jobItem) {
    String lookupKey = jobItem.getEntity().getLookUpKeyOnSourceDevice();

    if(StringUtils.isNotNullOrEmpty(lookupKey)) {
      try {
        String contactId = getContactIdStringForContact((ContactSyncEntity)jobItem.getEntity());

        ContentResolver resolver = context.getContentResolver();
        // Unbelievable, Motorola and HTC do not support deleting entries from call log: http://android-developers.narkive.com/W63HuY7c/delete-call-log-entry-exception
        int result = resolver.delete(ContactsContract.Contacts.CONTENT_URI, ContactsContract.Contacts._ID + " = ? ", new String[] { contactId });

        return result > 0;
      } catch(Exception e) {
        log.error("Could not delete Entry from Database: " + jobItem.getEntity(), e);
      }
    }

    return false;
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
  protected ContentValues mapEntityToPhoneNumberContentValues(ContactSyncEntity entity, Long rawContactId) {
    ContentValues values = new ContentValues();

    values.put(ContactsContract.CommonDataKinds.Phone.RAW_CONTACT_ID, rawContactId);
    values.put(ContactsContract.CommonDataKinds.Phone.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE);

    values.put(ContactsContract.CommonDataKinds.Phone.NUMBER, entity.getPhoneNumber());
    values.put(ContactsContract.CommonDataKinds.Phone.TYPE, ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE);
    // TODO: add custom type label
//    values.put(ContactsContract.CommonDataKinds.Phone.LABEL, entity.);
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

}
