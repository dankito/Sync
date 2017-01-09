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


  @NonNull
  @Override
  protected AndroidSyncModuleBase createSyncModuleToTest(Context appContext, IEntityManager entityManager) {
    return new AndroidContactsSyncModule(appContext, entityManager);
  }

  @Override
  protected void testEntity(SyncEntity entityToTest) {
    Assert.assertTrue(entityToTest instanceof ContactSyncEntity);

    ContactSyncEntity entity = (ContactSyncEntity)entityToTest;

    Assert.assertNotNull(entity.getDisplayName());
    Assert.assertNotNull(entity.getPhoneNumber());
  }

}
