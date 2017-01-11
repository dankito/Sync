package net.dankito.sync.synchronization.modules;

import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;

import net.dankito.sync.ContactSyncEntity;
import net.dankito.sync.ISyncModule;
import net.dankito.sync.SyncEntity;
import net.dankito.sync.SyncModuleConfiguration;
import net.dankito.sync.persistence.IEntityManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;

/**
 * Created by ganymed on 05/01/17.
 */

public class AndroidContactsSyncModule extends AndroidSyncModuleBase implements ISyncModule {

  private static final Logger log = LoggerFactory.getLogger(AndroidContactsSyncModule.class);


  public AndroidContactsSyncModule(Context context, IEntityManager entityManager) {
    super(context, entityManager);
  }

  @Override
  protected Uri[] getContentUris() {
    return new Uri[] { ContactsContract.RawContacts.CONTENT_URI };
  }

  @Override
  protected Uri getContentUriForContentObserver() {
    return ContactsContract.Contacts.CONTENT_URI;
  }

  @Override
  protected SyncEntity mapDatabaseEntryToSyncEntity(Cursor cursor, SyncModuleConfiguration syncModuleConfiguration) {
    ContactSyncEntity entity = new ContactSyncEntity(syncModuleConfiguration);

    entity.setLookUpKeyOnSourceDevice(readString(cursor, ContactsContract.RawContacts._ID));
    entity.setCreatedOnDevice(null); // TODO
    entity.setLastModifiedOnDevice(readDate(cursor, "version")); // TODO: don't know a better way to tell if raw contact has changed

    entity.setDisplayName(readString(cursor, ContactsContract.RawContacts.DISPLAY_NAME_PRIMARY));

    readPhoneNumbers(entity);

    readEmailAddresses(entity);

    readContactDetails(entity);

    return entity;
  }

  protected void readPhoneNumbers(ContactSyncEntity entity) {
    Cursor phones = context.getContentResolver().query(
        ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
        ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = " + entity.getLookUpKeyOnSourceDevice(),
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

  protected void readEmailAddresses(ContactSyncEntity entity) {
    Cursor emails = context.getContentResolver().query(
        ContactsContract.CommonDataKinds.Email.CONTENT_URI, null,
        ContactsContract.CommonDataKinds.Email.CONTACT_ID + " = " + entity.getLookUpKeyOnSourceDevice(),
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

  protected void readContactDetails(ContactSyncEntity entity) {
    readContactNameDetails(entity);

    entity.setNickname(readContactDetailString(entity.getLookUpKeyOnSourceDevice(), ContactsContract.CommonDataKinds.Nickname.CONTENT_ITEM_TYPE,
        ContactsContract.CommonDataKinds.Nickname.NAME));

    entity.setNote(readContactDetailString(entity.getLookUpKeyOnSourceDevice(), ContactsContract.CommonDataKinds.Note.CONTENT_ITEM_TYPE,
        ContactsContract.CommonDataKinds.Note.NOTE));

    entity.setWebsiteUrl(readContactDetailString(entity.getLookUpKeyOnSourceDevice(), ContactsContract.CommonDataKinds.Website.CONTENT_ITEM_TYPE,
        ContactsContract.CommonDataKinds.Website.URL)); // theoretically there's also a ContactsContract.CommonDataKinds.Website.TYPE, but it cannot be edited in UI
  }

  protected void readContactNameDetails(ContactSyncEntity entity) {
    String where = ContactsContract.Data.RAW_CONTACT_ID + " = ? AND " + ContactsContract.Data.MIMETYPE + " = ?";
    String[] whereParameters = new String[] { entity.getLookUpKeyOnSourceDevice(), ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE };

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

  protected String readContactDetailString(String entityId, String mimeType, String columnName) {
    String value = null;

    String[] columns = new String[] { columnName };
    String where = ContactsContract.Data.RAW_CONTACT_ID + " = ? AND " + ContactsContract.Data.MIMETYPE + " = ?";
    String[] whereParameters = new String[] { entityId, mimeType };

    Cursor details = context.getContentResolver().query(ContactsContract.Data.CONTENT_URI, columns, where, whereParameters, null);

    if(details.moveToFirst()) {
     value = readString(details, columns[0]);
    }

    details.close();

    return value;
  }


  @Override
  protected boolean addEntityToLocalDatabase(SyncEntity synchronizedEntity) {
    ContactSyncEntity entity = (ContactSyncEntity)synchronizedEntity;

    ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();
    ops.add(ContentProviderOperation.newInsert(ContactsContract.RawContacts.CONTENT_URI)
        .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, null)
        .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, null)
        .build());

    ContentResolver resolver = context.getContentResolver();

    try {
      ContentProviderResult[] results = resolver.applyBatch(ContactsContract.AUTHORITY, ops);

      if(results.length > 0 && results[0] != null) {
        String newRawContactIdString = results[0].uri.getLastPathSegment();
        entity.setLookUpKeyOnSourceDevice(newRawContactIdString);
      }
    } catch(Exception e) {
      log.error("Could not insert Contact into Database: " + entity, e);
    }

    if(entity.getLookUpKeyOnSourceDevice() != null) {
      return saveContactDetails(entity);
    }

    return false;
  }

  protected boolean saveContactDetails(ContactSyncEntity entity) {
    boolean result;
    Long rawContactId = Long.parseLong(entity.getLookUpKeyOnSourceDevice());

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

      Uri uri = context.getContentResolver().insert(ContactsContract.Data.CONTENT_URI, values);
      return wasInsertSuccessful(uri);
    } catch(Exception e) { log.error("Could not insert contact names into database for entity " + entity, e); }

    return false;
  }

  protected boolean savePhoneNumbers(ContactSyncEntity entity, Long rawContactId) {
    // TODO: save all phone numbers

    try {
      ContentValues values = new ContentValues();

      values.put(ContactsContract.CommonDataKinds.Phone.RAW_CONTACT_ID, rawContactId);
      values.put(ContactsContract.CommonDataKinds.Phone.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE);

      values.put(ContactsContract.CommonDataKinds.Phone.NUMBER, entity.getPhoneNumber());
      values.put(ContactsContract.CommonDataKinds.Phone.TYPE, ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE);
      // TODO: add custom type label
//    values.put(ContactsContract.CommonDataKinds.Phone.LABEL, entity.);

      Uri uri = context.getContentResolver().insert(ContactsContract.Data.CONTENT_URI, values);
      return wasInsertSuccessful(uri);
    } catch(Exception e) { log.error("Could not insert phone number into database for entity " + entity, e); }

    return false;
  }

  protected boolean saveEmailAddresses(ContactSyncEntity entity, Long rawContactId) {
    // TODO: save all email addresses

    try {
      ContentValues values = new ContentValues();

      values.put(ContactsContract.CommonDataKinds.Email.RAW_CONTACT_ID, rawContactId);
      values.put(ContactsContract.CommonDataKinds.Email.MIMETYPE, ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE);

      values.put(ContactsContract.CommonDataKinds.Email.ADDRESS, entity.getEmailAddress());
      values.put(ContactsContract.CommonDataKinds.Email.TYPE, ContactsContract.CommonDataKinds.Email.TYPE_HOME);
      // TODO: add custom type label
//    values.put(ContactsContract.CommonDataKinds.Email.LABEL, entity.);

      Uri uri = context.getContentResolver().insert(ContactsContract.Data.CONTENT_URI, values);
      return wasInsertSuccessful(uri);
    } catch(Exception e) { log.error("Could not insert email address into database for entity " + entity, e); }

    return false;
  }

  protected boolean saveContactDetail(Long rawContactId, String mimeType, String valueColumnName, String value) {
    return saveContactDetail(ContactsContract.Data.RAW_CONTACT_ID, rawContactId, ContactsContract.Data.MIMETYPE, mimeType, valueColumnName, value);
  }

  protected boolean saveContactDetail(String rawContactIdColumnName, Long rawContactId, String mimeTypeColumnName, String mimeType, String valueColumnName, String value) {
    try {
      ContentValues values = new ContentValues();

      values.put(rawContactIdColumnName, rawContactId);
      values.put(mimeTypeColumnName, mimeType);

      values.put(valueColumnName, value);

      Uri uri = context.getContentResolver().insert(ContactsContract.Data.CONTENT_URI, values);
      return wasInsertSuccessful(uri);
    } catch(Exception e) { log.error("Could not insert value '" + value + "' into mime type column '" + mimeTypeColumnName + "' for raw contact " + rawContactId, e); }

    return false;
  }


  @Override
  protected boolean updateEntityInLocalDatabase(SyncEntity synchronizedEntity) {
    return false;
  }

  @Override
  protected boolean deleteEntityFromLocalDatabase(SyncEntity synchronizedEntity) {
    return false;
  }

}
