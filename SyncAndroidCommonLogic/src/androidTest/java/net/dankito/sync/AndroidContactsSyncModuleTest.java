package net.dankito.sync;

import android.content.Context;
import android.support.annotation.NonNull;

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
  protected void testEntity(Entity entityToTest) {
    Assert.assertTrue(entityToTest instanceof ContactEntity);

    ContactEntity entity = (ContactEntity)entityToTest;

    Assert.assertNotNull(entity.getDisplayName());
    Assert.assertNotNull(entity.getPhoneNumber());
  }

}
