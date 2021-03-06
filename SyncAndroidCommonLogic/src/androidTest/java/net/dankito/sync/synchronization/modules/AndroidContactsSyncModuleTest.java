package net.dankito.sync.synchronization.modules;

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
import net.dankito.sync.localization.Localization;
import net.dankito.utils.IThreadPool;
import net.dankito.utils.StringUtils;

import org.junit.Assert;


public class AndroidContactsSyncModuleTest extends AndroidSyncModuleTestBase {

  protected static final String TEST_DISPLAY_NAME = "Nelson Rolihlahla Mandela";
  protected static final String TEST_NICK_NAME = "Madiba";
  protected static final String TEST_GIVEN_NAME = "Nelson";
  protected static final String TEST_MIDDLE_NAME = "Rolihlahla";
  protected static final String TEST_FAMILY_NAME = "Mandela";
  protected static final String TEST_PHONETIC_GIVEN_NAME = "Nelson Phonetic";
  protected static final String TEST_PHONETIC_MIDDLE_NAME = "Rolihlahla Phonetic";
  protected static final String TEST_PHONETIC_FAMILY_NAME = "Mandela Phonetic";
  protected static final String TEST_PHONE_NUMBER = "+27 (0)11 547 5600";
  protected static final PhoneNumberType TEST_PHONE_NUMBER_TYPE = PhoneNumberType.MOBILE;
  protected static final String TEST_EMAIL_ADDRESS = "nelson@nelsonmandela.org";
  protected static final EmailType TEST_EMAIL_ADDRESS_TYPE = EmailType.HOME;
  protected static final String TEST_NOTE = "One of my heroes";
  protected static final String TEST_WEBSITE_URL = "https://www.nelsonmandela.org";

  protected static final String TEST_UPDATED_DISPLAY_NAME = "Nelson Rolihlahla Mandela Updated";
  protected static final String TEST_UPDATED_NICK_NAME = "Madiba Updated";
  protected static final String TEST_UPDATED_GIVEN_NAME = "Nelson Updated";
  protected static final String TEST_UPDATED_MIDDLE_NAME = "Rolihlahla Updated";
  protected static final String TEST_UPDATED_FAMILY_NAME = "Mandela Updated";
  protected static final String TEST_UPDATED_PHONETIC_GIVEN_NAME = "Nelson Phonetic ";
  protected static final String TEST_UPDATED_PHONETIC_MIDDLE_NAME = "Rolihlahla Phonetic ";
  protected static final String TEST_UPDATED_PHONETIC_FAMILY_NAME = "Mandela Phonetic ";
  protected static final String TEST_UPDATED_PHONE_NUMBER = "+27 (0)11 547 5601";
  protected static final PhoneNumberType TEST_UPDATED_PHONE_NUMBER_TYPE = PhoneNumberType.MOBILE;
  protected static final String TEST_UPDATED_EMAIL_ADDRESS = "nelson_updated@nelsonmandela.org";
  protected static final EmailType TEST_UPDATED_EMAIL_ADDRESS_TYPE = EmailType.OTHER;
  protected static final String TEST_UPDATED_NOTE = "One of my heroes Updated";
  protected static final String TEST_UPDATED_WEBSITE_URL = "https://www.nelsonmandela.net";


  @NonNull
  @Override
  protected AndroidSyncModuleBase createSyncModuleToTest(Context context, Localization localization, IThreadPool threadPool) {
    return new AndroidContactsSyncModule(context, localization, threadPool);
  }

  @NonNull
  @Override
  protected SyncEntity createTestEntity() {
    ContactSyncEntity entity = new ContactSyncEntity();

    entity.setDisplayName(TEST_DISPLAY_NAME);
    entity.setNickname(TEST_NICK_NAME);
    entity.setGivenName(TEST_GIVEN_NAME);
    entity.setMiddleName(TEST_MIDDLE_NAME);
    entity.setFamilyName(TEST_FAMILY_NAME);
    entity.setPhoneticGivenName(TEST_PHONETIC_GIVEN_NAME);
    entity.setPhoneticMiddleName(TEST_PHONETIC_MIDDLE_NAME);
    entity.setPhoneticFamilyName(TEST_PHONETIC_FAMILY_NAME);
    entity.addPhoneNumber(new PhoneNumberSyncEntity(TEST_PHONE_NUMBER, TEST_PHONE_NUMBER_TYPE));
    entity.addEmailAddress(new EmailSyncEntity(TEST_EMAIL_ADDRESS, TEST_EMAIL_ADDRESS_TYPE));
    entity.setNote(TEST_NOTE);
    entity.setWebsiteUrl(TEST_WEBSITE_URL);

    return entity;
  }

  @Override
  protected void updateTestEntity(SyncEntity entityToUpdate) {
    ContactSyncEntity entity = (ContactSyncEntity)entityToUpdate;

    entity.setDisplayName(TEST_UPDATED_DISPLAY_NAME);
    entity.setNickname(TEST_UPDATED_NICK_NAME);
    entity.setGivenName(TEST_UPDATED_GIVEN_NAME);
    entity.setMiddleName(TEST_UPDATED_MIDDLE_NAME);
    entity.setFamilyName(TEST_UPDATED_FAMILY_NAME);
    entity.setPhoneticGivenName(TEST_UPDATED_PHONETIC_GIVEN_NAME);
    entity.setPhoneticMiddleName(TEST_UPDATED_PHONETIC_MIDDLE_NAME);
    entity.setPhoneticFamilyName(TEST_UPDATED_PHONETIC_FAMILY_NAME);

    PhoneNumberSyncEntity phoneNumber = entity.getPhoneNumbers().get(0);
    phoneNumber.setNumber(TEST_UPDATED_PHONE_NUMBER);
    phoneNumber.setType(TEST_UPDATED_PHONE_NUMBER_TYPE);

    EmailSyncEntity email = entity.getEmailAddresses().get(0);
    email.setAddress(TEST_UPDATED_EMAIL_ADDRESS);
    email.setType(TEST_UPDATED_EMAIL_ADDRESS_TYPE);

    entity.setNote(TEST_UPDATED_NOTE);
    entity.setWebsiteUrl(TEST_UPDATED_WEBSITE_URL);
  }


  @Override
  protected void testReadEntity(SyncEntity entityToTest) {
    Assert.assertTrue(entityToTest instanceof ContactSyncEntity);

    ContactSyncEntity entity = (ContactSyncEntity)entityToTest;

    Assert.assertNotNull(entity.getDisplayName());

    Assert.assertNotEquals(0, entity.getPhoneNumbers());
    for(PhoneNumberSyncEntity phoneNumber : entity.getPhoneNumbers()) {
      Assert.assertNotNull(phoneNumber.getNumber());
      Assert.assertNotNull(phoneNumber.getType());
    }
  }

  @Override
  protected void testIfEntryHasSuccessfullyBeenAdded(SyncEntity entity) {
    Assert.assertTrue(StringUtils.isNotNullOrEmpty(entity.getLocalLookupKey()));

    testContactNames(entity);

    testContactPhoneNumbers(entity);

    testContactEmailAddresses(entity);

    testContactDetail(entity, ContactsContract.CommonDataKinds.Nickname.CONTENT_ITEM_TYPE, ContactsContract.CommonDataKinds.Nickname.NAME, TEST_NICK_NAME);

    testContactDetail(entity, ContactsContract.CommonDataKinds.Note.CONTENT_ITEM_TYPE, ContactsContract.CommonDataKinds.Note.NOTE, TEST_NOTE);

    testContactDetail(entity, ContactsContract.CommonDataKinds.Website.CONTENT_ITEM_TYPE, ContactsContract.CommonDataKinds.Website.URL, TEST_WEBSITE_URL);
  }

  protected void testContactNames(SyncEntity entity) {
    Cursor cursor = getCursorForEntity(entity, ContactsContract.Data.CONTENT_URI, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE);

    Assert.assertTrue(cursor.moveToFirst()); // does entry with this look up key exist?

    Assert.assertEquals(TEST_DISPLAY_NAME, underTest.readString(cursor, ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME));

    Assert.assertEquals(TEST_GIVEN_NAME, underTest.readString(cursor, ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME));
    Assert.assertEquals(TEST_MIDDLE_NAME, underTest.readString(cursor, ContactsContract.CommonDataKinds.StructuredName.MIDDLE_NAME));
    Assert.assertEquals(TEST_FAMILY_NAME, underTest.readString(cursor, ContactsContract.CommonDataKinds.StructuredName.FAMILY_NAME));

    Assert.assertEquals(TEST_PHONETIC_GIVEN_NAME, underTest.readString(cursor, ContactsContract.CommonDataKinds.StructuredName.PHONETIC_GIVEN_NAME));
    Assert.assertEquals(TEST_PHONETIC_MIDDLE_NAME, underTest.readString(cursor, ContactsContract.CommonDataKinds.StructuredName.PHONETIC_MIDDLE_NAME));
    Assert.assertEquals(TEST_PHONETIC_FAMILY_NAME, underTest.readString(cursor, ContactsContract.CommonDataKinds.StructuredName.PHONETIC_FAMILY_NAME));
  }

  protected void testContactPhoneNumbers(SyncEntity entity) {
    Cursor cursor = getCursorForEntity(entity, ContactsContract.Data.CONTENT_URI, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE);

    Assert.assertTrue(cursor.moveToFirst());

    Assert.assertEquals(TEST_PHONE_NUMBER, underTest.readString(cursor, ContactsContract.CommonDataKinds.Phone.NUMBER));
    Assert.assertEquals(TEST_PHONE_NUMBER_TYPE,
        ((AndroidContactsSyncModule)underTest).parsePhoneNumberType(underTest.readInteger(cursor, ContactsContract.CommonDataKinds.Phone.TYPE)));
  }

  protected void testContactEmailAddresses(SyncEntity entity) {
    Cursor cursor = getCursorForEntity(entity, ContactsContract.Data.CONTENT_URI, ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE);

    Assert.assertTrue(cursor.moveToFirst());

    Assert.assertEquals(TEST_EMAIL_ADDRESS, underTest.readString(cursor, ContactsContract.CommonDataKinds.Email.ADDRESS));
    Assert.assertEquals(TEST_EMAIL_ADDRESS_TYPE,
        ((AndroidContactsSyncModule)underTest).parseEmailType(underTest.readInteger(cursor, ContactsContract.CommonDataKinds.Email.TYPE)));
  }

  protected void testContactDetail(SyncEntity entity, String mimeType, String detailColumnName, String expectedValue) {
    Cursor cursor = getCursorForEntity(entity, ContactsContract.Data.CONTENT_URI, mimeType);

    Assert.assertTrue(cursor.moveToFirst());

    Assert.assertEquals(expectedValue, underTest.readString(cursor, detailColumnName));
  }


  @Override
  protected void testIfEntryHasSuccessfullyBeenUpdated(SyncEntity entity) {
    Assert.assertTrue(StringUtils.isNotNullOrEmpty(entity.getLocalLookupKey()));

    // comparing values makes currently no sense as Android creates a new Raw Contact when the original Raw Contact is edited too much
    testUpdatedContactNames(entity);

    testUpdatedContactPhoneNumbers(entity);

    testUpdatedContactEmailAddresses(entity);

    testContactDetail(entity, ContactsContract.CommonDataKinds.Nickname.CONTENT_ITEM_TYPE, ContactsContract.CommonDataKinds.Nickname.NAME, TEST_UPDATED_NICK_NAME);

    testContactDetail(entity, ContactsContract.CommonDataKinds.Note.CONTENT_ITEM_TYPE, ContactsContract.CommonDataKinds.Note.NOTE, TEST_UPDATED_NOTE);

    testContactDetail(entity, ContactsContract.CommonDataKinds.Website.CONTENT_ITEM_TYPE, ContactsContract.CommonDataKinds.Website.URL, TEST_UPDATED_WEBSITE_URL);
  }

  protected void testUpdatedContactNames(SyncEntity entity) {
    Cursor cursor = getCursorForEntity(entity, ContactsContract.Data.CONTENT_URI, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE);

    Assert.assertTrue(cursor.moveToFirst()); // does entry with this look up key exist?

    Assert.assertEquals(TEST_UPDATED_DISPLAY_NAME, underTest.readString(cursor, ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME));

    Assert.assertEquals(TEST_UPDATED_GIVEN_NAME, underTest.readString(cursor, ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME));
    Assert.assertEquals(TEST_UPDATED_MIDDLE_NAME, underTest.readString(cursor, ContactsContract.CommonDataKinds.StructuredName.MIDDLE_NAME));
    Assert.assertEquals(TEST_UPDATED_FAMILY_NAME, underTest.readString(cursor, ContactsContract.CommonDataKinds.StructuredName.FAMILY_NAME));

    Assert.assertEquals(TEST_UPDATED_PHONETIC_GIVEN_NAME, underTest.readString(cursor, ContactsContract.CommonDataKinds.StructuredName.PHONETIC_GIVEN_NAME));
    Assert.assertEquals(TEST_UPDATED_PHONETIC_MIDDLE_NAME, underTest.readString(cursor, ContactsContract.CommonDataKinds.StructuredName.PHONETIC_MIDDLE_NAME));
    Assert.assertEquals(TEST_UPDATED_PHONETIC_FAMILY_NAME, underTest.readString(cursor, ContactsContract.CommonDataKinds.StructuredName.PHONETIC_FAMILY_NAME));
  }

  protected void testUpdatedContactPhoneNumbers(SyncEntity entity) {
    Cursor cursor = getCursorForEntity(entity, ContactsContract.Data.CONTENT_URI, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE);

    Assert.assertTrue(cursor.moveToFirst());

    Assert.assertEquals(TEST_UPDATED_PHONE_NUMBER, underTest.readString(cursor, ContactsContract.CommonDataKinds.Phone.NUMBER));
    Assert.assertEquals(TEST_UPDATED_PHONE_NUMBER_TYPE,
        ((AndroidContactsSyncModule)underTest).parsePhoneNumberType(underTest.readInteger(cursor, ContactsContract.CommonDataKinds.Phone.TYPE)));
  }

  protected void testUpdatedContactEmailAddresses(SyncEntity entity) {
    Cursor cursor = getCursorForEntity(entity, ContactsContract.Data.CONTENT_URI, ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE);

    Assert.assertTrue(cursor.moveToFirst());

    Assert.assertEquals(TEST_UPDATED_EMAIL_ADDRESS, underTest.readString(cursor, ContactsContract.CommonDataKinds.Email.ADDRESS));
    Assert.assertEquals(TEST_UPDATED_EMAIL_ADDRESS_TYPE,
        ((AndroidContactsSyncModule)underTest).parseEmailType(underTest.readInteger(cursor, ContactsContract.CommonDataKinds.Email.TYPE)));
  }


  @NonNull
  protected String getIdColumnForEntity() {
    return ContactsContract.RawContacts._ID;
  }


  protected Cursor getCursorForEntity(SyncEntity entity, Uri contentUri, String mimeType) {
    return appContext.getContentResolver().query(
        contentUri,
        null, // Which columns to return
        ContactsContract.Data.RAW_CONTACT_ID + " = ? AND " + ContactsContract.Data.MIMETYPE + " = ?",       // Which rows to return (all rows)
        new String[] { entity.getLocalLookupKey(), mimeType },       // Selection arguments (none)
        null        // Ordering
    );
  }

}
