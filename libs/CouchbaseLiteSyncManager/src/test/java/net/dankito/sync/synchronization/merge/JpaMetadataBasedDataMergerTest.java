package net.dankito.sync.synchronization.merge;

import com.couchbase.lite.CouchbaseLiteException;

import net.dankito.sync.ContactSyncEntity;
import net.dankito.sync.PhoneNumberSyncEntity;
import net.dankito.sync.PhoneNumberType;
import net.dankito.sync.persistence.CouchbaseLiteEntityManagerBase;
import net.dankito.sync.persistence.CouchbaseLiteEntityManagerJava;
import net.dankito.sync.persistence.EntityManagerConfiguration;
import net.dankito.utils.services.JavaFileStorageService;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;


public class JpaMetadataBasedDataMergerTest {

  protected static final String TEST_UPDATED_DISPLAY_NAME = "Nelson Rolihlahla Mandela Updated";
  protected static final String TEST_UPDATED_NICK_NAME = "Madiba Updated";
  protected static final String TEST_UPDATED_GIVEN_NAME = "Nelson Updated";
  protected static final String TEST_UPDATED_MIDDLE_NAME = "Rolihlahla Updated";
  protected static final String TEST_UPDATED_FAMILY_NAME = "Mandela Updated";
  protected static final String TEST_UPDATED_PHONETIC_GIVEN_NAME = "Nelson Phonetic ";
  protected static final String TEST_UPDATED_PHONETIC_MIDDLE_NAME = "Rolihlahla Phonetic ";
  protected static final String TEST_UPDATED_PHONETIC_FAMILY_NAME = "Mandela Phonetic ";
  protected static final String TEST_UPDATED_PHONE_NUMBER = "+27 (0)11 547 5601";
  protected static final PhoneNumberType TEST_UPDATED_PHONE_NUMBER_TYPE = PhoneNumberType.WORK;
  protected static final String TEST_UPDATED_EMAIL_ADDRESS = "nelson_updated@nelsonmandela.org";
  protected static final String TEST_UPDATED_NOTE = "One of my heroes Updated";
  protected static final String TEST_UPDATED_WEBSITE_URL = "https://www.nelsonmandela.net";


  protected IDataMerger underTest;

  protected CouchbaseLiteEntityManagerBase entityManager;


  @Before
  public void setUp() throws Exception {
    entityManager = new CouchbaseLiteEntityManagerJava(new EntityManagerConfiguration("testData", 1));

    underTest = new JpaMetadataBasedDataMerger(entityManager);
  }

  @After
  public void tearDown() throws CouchbaseLiteException {
    new JavaFileStorageService().deleteFolderRecursively(new File(entityManager.getDatabasePath()).getParent());
  }


  @Test
  public void updateProperty() throws Exception {
    ContactSyncEntity updateSource = createSourceEntity();

    ContactSyncEntity updateSink = new ContactSyncEntity();
    entityManager.persistEntity(updateSink);


    underTest.mergeEntityData(updateSink, updateSource);


    Assert.assertNotNull(updateSink.getId());
    Assert.assertNull(updateSource.getId());
    Assert.assertNotNull(updateSink.getCreatedOn());
    Assert.assertNull(updateSource.getCreatedOn());
    Assert.assertNotNull(updateSink.getModifiedOn());
    Assert.assertNull(updateSource.getModifiedOn());
    Assert.assertNotNull(updateSink.getVersion());
    Assert.assertNull(updateSource.getVersion());

    Assert.assertEquals(TEST_UPDATED_DISPLAY_NAME, updateSink.getDisplayName());
    Assert.assertEquals(TEST_UPDATED_NICK_NAME, updateSink.getNickname());

    Assert.assertEquals(TEST_UPDATED_GIVEN_NAME, updateSink.getGivenName());
    Assert.assertEquals(TEST_UPDATED_MIDDLE_NAME, updateSink.getMiddleName());
    Assert.assertEquals(TEST_UPDATED_FAMILY_NAME, updateSink.getFamilyName());

    Assert.assertEquals(TEST_UPDATED_PHONETIC_GIVEN_NAME, updateSink.getPhoneticGivenName());
    Assert.assertEquals(TEST_UPDATED_PHONETIC_MIDDLE_NAME, updateSink.getPhoneticMiddleName());
    Assert.assertEquals(TEST_UPDATED_PHONETIC_FAMILY_NAME, updateSink.getPhoneticFamilyName());

    Assert.assertEquals(1, updateSink.getPhoneNumbers().size());
    Assert.assertEquals(TEST_UPDATED_PHONE_NUMBER, updateSink.getPhoneNumbers().get(0).getNumber());
    Assert.assertEquals(TEST_UPDATED_PHONE_NUMBER_TYPE, updateSink.getPhoneNumbers().get(0).getType());

    Assert.assertEquals(TEST_UPDATED_EMAIL_ADDRESS, updateSink.getEmailAddress());

    Assert.assertEquals(TEST_UPDATED_NOTE, updateSink.getNote());
    Assert.assertEquals(TEST_UPDATED_WEBSITE_URL, updateSink.getWebsiteUrl());
  }


  protected ContactSyncEntity createSourceEntity() {
    ContactSyncEntity sourceEntity = new ContactSyncEntity();

    sourceEntity.setDisplayName(TEST_UPDATED_DISPLAY_NAME);
    sourceEntity.setNickname(TEST_UPDATED_NICK_NAME);

    sourceEntity.setGivenName(TEST_UPDATED_GIVEN_NAME);
    sourceEntity.setMiddleName(TEST_UPDATED_MIDDLE_NAME);
    sourceEntity.setFamilyName(TEST_UPDATED_FAMILY_NAME);
    sourceEntity.setPhoneticGivenName(TEST_UPDATED_PHONETIC_GIVEN_NAME);
    sourceEntity.setPhoneticMiddleName(TEST_UPDATED_PHONETIC_MIDDLE_NAME);
    sourceEntity.setPhoneticFamilyName(TEST_UPDATED_PHONETIC_FAMILY_NAME);

    sourceEntity.addPhoneNumber(new PhoneNumberSyncEntity(TEST_UPDATED_PHONE_NUMBER, TEST_UPDATED_PHONE_NUMBER_TYPE));
    sourceEntity.setEmailAddress(TEST_UPDATED_EMAIL_ADDRESS);

    sourceEntity.setNote(TEST_UPDATED_NOTE);
    sourceEntity.setWebsiteUrl(TEST_UPDATED_WEBSITE_URL);

    return sourceEntity;
  }

}