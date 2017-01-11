package net.dankito.sync.synchronization.modules;

import android.content.Context;
import android.support.annotation.NonNull;

import net.dankito.sync.ContactSyncEntity;
import net.dankito.sync.SyncEntity;
import net.dankito.sync.persistence.IEntityManager;

import org.junit.Assert;

/**
 * Created by ganymed on 05/01/17.
 */

public class AndroidContactsSyncModuleTest extends AndroidSyncModuleTestBase {

  protected static final String TEST_DISPLAY_NAME = "Nelson Rolihlahla Mandela";
  protected static final String TEST_NICK_NAME = "Madiba";
  protected static final String TEST_GIVEN_NAME = "Nelson";
  protected static final String TEST_MIDDLE_NAME = "Rolihlahla";
  protected static final String TEST_FAMILY_NAME = "Mandela";
  protected static final String TEST_PHONETIC_GIVEN_NAME = "Nelson Phonetic";
  protected static final String TEST_PHONETIC_MIDDLE_NAME = "Rolihlahla Phonetic";
  protected static final String TEST_PHONETIC_FAMILY_NAME = "Mandela Phonetic";
  protected static final String TEST_PHONE = "+27 (0)11 547 5600";
  protected static final String TEST_EMAIL_ADDRESS = "nelson@nelsonmandela.org";
  protected static final String TEST_WEBSITE_URL = "https://www.nelsonmandela.org";


  @NonNull
  @Override
  protected AndroidSyncModuleBase createSyncModuleToTest(Context appContext, IEntityManager entityManager) {
    return new AndroidContactsSyncModule(appContext, entityManager);
  }

  @NonNull
  @Override
  protected SyncEntity createTestEntity() {
    ContactSyncEntity entity = new ContactSyncEntity(null);

    entity.setDisplayName(TEST_DISPLAY_NAME);
    entity.setNickname(TEST_NICK_NAME);
    entity.setGivenName(TEST_GIVEN_NAME);
    entity.setMiddleName(TEST_MIDDLE_NAME);
    entity.setFamilyName(TEST_FAMILY_NAME);
    entity.setPhoneticGivenName(TEST_PHONETIC_GIVEN_NAME);
    entity.setPhoneticMiddleName(TEST_PHONETIC_MIDDLE_NAME);
    entity.setPhoneticFamilyName(TEST_PHONETIC_FAMILY_NAME);
    entity.setPhoneNumber(TEST_PHONE);
    entity.setEmailAddress(TEST_EMAIL_ADDRESS);
    entity.setWebsiteUrl(TEST_WEBSITE_URL);

    return entity;
  }

  @Override
  protected void updateTestEntity(SyncEntity entityToUpdate) {

  }

  @Override
  protected void testEntity(SyncEntity entityToTest) {
    Assert.assertTrue(entityToTest instanceof ContactSyncEntity);

    ContactSyncEntity entity = (ContactSyncEntity)entityToTest;

    Assert.assertNotNull(entity.getDisplayName());
    Assert.assertNotNull(entity.getPhoneNumber());
  }

}
