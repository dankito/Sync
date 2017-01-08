package net.dankito.sync.synchronization.modules;

import android.content.Context;

import net.dankito.sync.CallLogSyncEntity;
import net.dankito.sync.SyncEntity;

import org.junit.Assert;

/**
 * Created by ganymed on 05/01/17.
 */

public class AndroidCallLogSyncModuleTest extends AndroidSyncModuleTestBase {


  @Override
  protected AndroidSyncModuleBase createSyncModuleToTest(Context appContext) {
    return new AndroidCallLogSyncModule(appContext);
  }

  @Override
  protected void testEntity(SyncEntity entityToTest) {
    Assert.assertTrue(entityToTest instanceof CallLogSyncEntity);

    CallLogSyncEntity entity = (CallLogSyncEntity)entityToTest;

    Assert.assertNotNull(entity.getNumber());
    Assert.assertTrue(entity.getNormalizedNumber() != null || entity.getNumber().length() == 0); // for incoming unknown numbers getNumber() is an empty String and getNormalizedNumber() is null
    Assert.assertNotNull(entity.getDate());
    Assert.assertTrue(entity.getDurationInSeconds() >= 0);
    Assert.assertNotNull(entity.getType());
  }

}
