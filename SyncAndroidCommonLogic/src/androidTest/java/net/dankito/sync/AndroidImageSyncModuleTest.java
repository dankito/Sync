package net.dankito.sync;

import android.content.Context;

import org.junit.Assert;

/**
 * Created by ganymed on 05/01/17.
 */

public class AndroidImageSyncModuleTest extends AndroidSyncModuleTestBase {


  @Override
  protected AndroidSyncModuleBase createSyncModuleToTest(Context appContext) {
    return new AndroidImageSyncModule(appContext);
  }

  @Override
  protected void testEntity(SyncEntity entityToTest) {
    Assert.assertTrue(entityToTest instanceof ImageFileSyncEntity);

    ImageFileSyncEntity entity = (ImageFileSyncEntity)entityToTest;

    Assert.assertNotNull(entity.getFilePath());
    Assert.assertNotNull(entity.getName());
    Assert.assertNotNull(entity.getMimeType());
    Assert.assertNotNull(entity.getImageTakenOn());
  }

}
