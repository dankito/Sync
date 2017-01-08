package net.dankito.sync.synchronization.modules;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;

import net.dankito.sync.ContactSyncEntity;
import net.dankito.sync.ISyncModule;
import net.dankito.sync.SyncEntity;
import net.dankito.sync.SyncModuleConfiguration;

/**
 * Created by ganymed on 05/01/17.
 */

public class AndroidContactsSyncModule extends AndroidSyncModuleBase implements ISyncModule {


  public AndroidContactsSyncModule(Context context) {
    super(context);
  }

  @Override
  protected Uri[] getContentUris() {
    return new Uri[] { ContactsContract.CommonDataKinds.Phone.CONTENT_URI };
  }

  @Override
  protected SyncEntity deserializeDatabaseEntry(Cursor cursor, SyncModuleConfiguration syncModuleConfiguration) {
    ContactSyncEntity entity = new ContactSyncEntity(syncModuleConfiguration);

    entity.setIdOnSourceDevice(readString(cursor, "raw_contact_id"));
    entity.setCreatedOn(null); // TODO
    entity.setModifiedOn(readDate(cursor, ContactsContract.Contacts.CONTACT_LAST_UPDATED_TIMESTAMP));

    entity.setDisplayName(readString(cursor, ContactsContract.Contacts.DISPLAY_NAME));

    boolean hasPhoneNumber = readBoolean(cursor, ContactsContract.Contacts.HAS_PHONE_NUMBER);
    if(hasPhoneNumber) {
      readPhoneNumbers(entity);
    }

    readEmailAddresses(entity);

    readContactDetails(entity);

    return entity;
  }

  protected void readPhoneNumbers(ContactSyncEntity entity) {
    Cursor phones = context.getContentResolver().query(
        ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
        ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = " + entity.getIdOnSourceDevice(),
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
        ContactsContract.CommonDataKinds.Email.CONTACT_ID + " = " + entity.getIdOnSourceDevice(),
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

    entity.setNickname(readContactDetailString(entity.getIdOnSourceDevice(), ContactsContract.CommonDataKinds.Nickname.CONTENT_ITEM_TYPE,
        ContactsContract.CommonDataKinds.Nickname.NAME));

    entity.setNote(readContactDetailString(entity.getIdOnSourceDevice(), ContactsContract.CommonDataKinds.Note.CONTENT_ITEM_TYPE,
        ContactsContract.CommonDataKinds.Note.NOTE));

    entity.setWebsiteUrl(readContactDetailString(entity.getIdOnSourceDevice(), ContactsContract.CommonDataKinds.Website.CONTENT_ITEM_TYPE,
        ContactsContract.CommonDataKinds.Website.URL)); // theoretically there's also a ContactsContract.CommonDataKinds.Website.TYPE, but it cannot be edited in UI
  }

  protected void readContactNameDetails(ContactSyncEntity entity) {
    String where = ContactsContract.Data.RAW_CONTACT_ID + " = ? AND " + ContactsContract.Data.MIMETYPE + " = ?";
    String[] whereParameters = new String[] { entity.getIdOnSourceDevice(), ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE };

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

}
