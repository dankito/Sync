package net.dankito.sync.synchronization.modules;

import android.content.Context;
import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import net.dankito.sync.ContactSyncEntity;
import net.dankito.sync.ReadEntitiesCallback;
import net.dankito.sync.SyncEntity;
import net.dankito.sync.SyncEntityState;
import net.dankito.sync.persistence.EntityManagerStub;
import net.dankito.sync.persistence.IEntityManager;
import net.dankito.sync.synchronization.SyncEntityChangeListener;
import net.dankito.utils.StringUtils;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.text.ParseException;
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

  @NonNull
  protected abstract SyncEntity createTestEntity();

  protected abstract void updateTestEntity(SyncEntity entityToUpdate);


  protected abstract void testReadEntity(SyncEntity entityToTest);

  protected abstract void testIfEntryHasSuccessfullyBeenAdded(SyncEntity entity);

  protected abstract void testIfEntryHasSuccessfullyBeenUpdated(SyncEntity entity);

  @NonNull
  protected abstract String getIdColumnForEntity();


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

      testReadEntity(entity);
    }
  }


  @Test
  public void synchronizedNewEntity_EntityGetsAdded() throws ParseException {
    SyncEntity entity = createTestEntityAndAddToDeleteAfterTest();

    underTest.synchronizedEntityRetrieved(entity, SyncEntityState.CREATED);

    testIfEntryHasSuccessfullyBeenAdded(entity);
  }


  @Test
  public void synchronizedUpdatedEntity_EntityGetsUpdated() throws ParseException {
    SyncEntity entity = createTestEntityAndAddToDeleteAfterTest();

    underTest.addEntityToLocalDatabase(entity);

    updateTestEntity(entity);


    underTest.synchronizedEntityRetrieved(entity, SyncEntityState.UPDATED);


    testIfEntryHasSuccessfullyBeenUpdated(entity);
  }


  @Test
  public void synchronizedDeletedEntity_EntityGetsRemoved() throws ParseException {
    SyncEntity entity = createTestEntityAndAddToDeleteAfterTest();

    underTest.addEntityToLocalDatabase(entity);


    underTest.synchronizedEntityRetrieved(entity, SyncEntityState.DELETED);


    testIfEntryHasSuccessfullyBeenRemoved(entity);
  }


  @Test
  public void addSyncEntityChangeListener_EntityGetsAdded_ListenerGetsCalled() {
    final List<SyncEntity> changedEntities = new ArrayList<>();
    final CountDownLatch countDownLatch = new CountDownLatch(1);

    underTest.addSyncEntityChangeListener(new SyncEntityChangeListener() {
      @Override
      public void entityChanged(SyncEntity entity) {
        changedEntities.add(entity);
        countDownLatch.countDown();
      }
    });

    SyncEntity syncEntity = createTestEntityAndAddToDeleteAfterTest();

    underTest.addEntityToLocalDatabase(syncEntity);

    try { countDownLatch.await(3, TimeUnit.SECONDS); } catch(Exception ignored) { }

    Assert.assertTrue(changedEntities.size() > 0);
    // TODO: also check added Entity
  }

  @Test
  public void addSyncEntityChangeListener_EntityGetsUpdated_ListenerGetsCalled() {
    final List<SyncEntity> changedEntities = new ArrayList<>();
    final CountDownLatch countDownLatch = new CountDownLatch(1);

    SyncEntity syncEntity = createTestEntityAndAddToDeleteAfterTest();
    underTest.addEntityToLocalDatabase(syncEntity);

    underTest.addSyncEntityChangeListener(new SyncEntityChangeListener() {
      @Override
      public void entityChanged(SyncEntity entity) {
        changedEntities.add(entity);
        countDownLatch.countDown();
      }
    });

    updateTestEntity(syncEntity);
    underTest.updateEntityInLocalDatabase(syncEntity);

    try { countDownLatch.await(3, TimeUnit.SECONDS); } catch(Exception ignored) { }

    Assert.assertTrue(changedEntities.size() > 0);
    // TODO: also check added Entity
  }

  @Test
  public void addSyncEntityChangeListener_EntityGetsRemoved_ListenerGetsCalled() {
    final List<SyncEntity> changedEntities = new ArrayList<>();
    final CountDownLatch countDownLatch = new CountDownLatch(1);

    SyncEntity syncEntity = createTestEntityAndAddToDeleteAfterTest();
    underTest.addEntityToLocalDatabase(syncEntity);

    underTest.addSyncEntityChangeListener(new SyncEntityChangeListener() {
      @Override
      public void entityChanged(SyncEntity entity) {
        changedEntities.add(entity);
        countDownLatch.countDown();
      }
    });

    underTest.deleteEntityFromLocalDatabase(syncEntity);

    try { countDownLatch.await(3, TimeUnit.SECONDS); } catch(Exception ignored) { }

    Assert.assertEquals(1, changedEntities.size());
    // TODO: also check added Entity
  }


  protected void testIfEntryHasSuccessfullyBeenRemoved(SyncEntity entity) {
    Assert.assertTrue(StringUtils.isNotNullOrEmpty(entity.getLookUpKeyOnSourceDevice()));

    Cursor cursor = getCursorForEntity(entity);

    Assert.assertFalse(cursor.moveToFirst()); // assert entity does not exist anymore
  }


  protected Cursor getCursorForEntity(SyncEntity entity) {
    return appContext.getContentResolver().query(
        underTest.getContentUris()[0],
        null, // Which columns to return
        getIdColumnForEntity() + " = ?",       // Which rows to return (all rows)
        new String[] { entity.getLookUpKeyOnSourceDevice() },       // Selection arguments (none)
        null        // Ordering
    );
  }


  protected SyncEntity createTestEntityAndAddToDeleteAfterTest() {
    SyncEntity testEntity = createTestEntity();

    addEntityToDeleteAfterTest(testEntity);

    return testEntity;
  }

  protected void addEntityToDeleteAfterTest(SyncEntity entity) {
    entitiesToDeleteAfterTest.add(entity);
  }


}
