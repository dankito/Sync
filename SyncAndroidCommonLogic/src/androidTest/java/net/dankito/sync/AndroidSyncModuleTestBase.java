package net.dankito.sync;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Created by ganymed on 05/01/17.
 */

@RunWith(AndroidJUnit4.class)
public abstract class AndroidSyncModuleTestBase {

  protected AndroidSyncModuleBase underTest;

  protected Context appContext = InstrumentationRegistry.getTargetContext();


  @Before
  public void setUp() {
    underTest = createSyncModuleToTest(appContext);
  }

  @NonNull
  protected abstract AndroidSyncModuleBase createSyncModuleToTest(Context appContext);


  @Test
  public void readAllEntitiesAsync() {
    final List<SyncEntity> result = new ArrayList<>();
    final CountDownLatch countDownLatch = new CountDownLatch(1);

    underTest.readAllEntitiesAsync(null, new ReadEntitiesCallback() {
      @Override
      public void done(List<SyncEntity> entities) {
        result.addAll(entities);
        countDownLatch.countDown();
      }
    });

    try { countDownLatch.await(7, TimeUnit.SECONDS); } catch(Exception e) { }

    Assert.assertNotEquals(0, result.size());

    for(SyncEntity entity : result) {
      Assert.assertNotNull(entity.getIdOnSourceDevice());
      Assert.assertNotNull(entity.getCreatedOn());
      Assert.assertNotNull(entity.getModifiedOn());

      testEntity(entity);
    }
  }

  protected abstract void testEntity(SyncEntity entityToTest);


}
