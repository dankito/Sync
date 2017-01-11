package net.dankito.sync.synchronization.modules;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import net.dankito.sync.ContactSyncEntity;
import net.dankito.sync.ReadEntitiesCallback;
import net.dankito.sync.SyncEntity;
import net.dankito.sync.persistence.EntityManagerStub;
import net.dankito.sync.persistence.IEntityManager;

import org.junit.After;
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

  protected IEntityManager entityManager;

  protected List<SyncEntity> entitiesToDeleteAfterTest = new ArrayList<>();


  @Before
  public void setUp() {
    entityManager = new EntityManagerStub();

    underTest = createSyncModuleToTest(appContext, entityManager);
  }

  @After
  public void tearDown() {
    for(SyncEntity entityToDelete : entitiesToDeleteAfterTest) {
      underTest.deleteEntityFromLocalDatabase(entityToDelete);
    }
  }


  @NonNull
  protected abstract AndroidSyncModuleBase createSyncModuleToTest(Context appContext, IEntityManager entityManager);


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
      Assert.assertNotNull(entity.getLookUpKeyOnSourceDevice());
      if(entity instanceof ContactSyncEntity == false) { // is null for ContactSyncEntities
        Assert.assertNotNull(entity.getCreatedOnDevice());
      }
      Assert.assertNotNull(entity.getLastModifiedOnDevice());

      testEntity(entity);
    }
  }

  protected abstract void testEntity(SyncEntity entityToTest);


  protected void addEntityToDeleteAfterTest(SyncEntity entity) {
    entitiesToDeleteAfterTest.add(entity);
  }


}
