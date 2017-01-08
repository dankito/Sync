package net.dankito.sync.synchronization.modules;

import android.content.Context;
import android.support.annotation.NonNull;

import net.dankito.sync.ContactSyncEntity;
import net.dankito.sync.SyncEntity;

import org.junit.Assert;

/**
 * Created by ganymed on 05/01/17.
 */

public class AndroidContactsSyncModuleTest extends AndroidSyncModuleTestBase {


  @NonNull
  @Override
  protected AndroidSyncModuleBase createSyncModuleToTest(Context appContext) {
    return new AndroidContactsSyncModule(appContext);
  }

  @Override
  protected void testEntity(SyncEntity entityToTest) {
    Assert.assertTrue(entityToTest instanceof ContactSyncEntity);

    ContactSyncEntity entity = (ContactSyncEntity)entityToTest;

    Assert.assertNotNull(entity.getDisplayName());
    Assert.assertNotNull(entity.getPhoneNumber());
  }

}
