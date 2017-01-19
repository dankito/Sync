package net.dankito.sync.synchronization.modules;

import android.content.Context;
import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import net.dankito.android.util.services.IPermissionsManager;
import net.dankito.sync.ContactSyncEntity;
import net.dankito.sync.Device;
import net.dankito.sync.OsType;
import net.dankito.sync.SyncEntity;
import net.dankito.sync.SyncEntityState;
import net.dankito.sync.SyncJobItem;
import net.dankito.sync.SyncModuleConfiguration;
import net.dankito.sync.localization.Localization;
import net.dankito.sync.persistence.EntityManagerStub;
import net.dankito.sync.persistence.IEntityManager;
import net.dankito.sync.synchronization.SyncEntityChange;
import net.dankito.sync.synchronization.SyncEntityChangeListener;
import net.dankito.sync.synchronization.modules.util.PermissionsManagerStub;
import net.dankito.utils.IThreadPool;
import net.dankito.utils.StringUtils;
import net.dankito.utils.ThreadPool;

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


  protected SyncModuleConfiguration syncModuleConfiguration;

  protected Context appContext = InstrumentationRegistry.getTargetContext();

  protected IEntityManager entityManager;

  protected IThreadPool threadPool;

  protected Device localDevice;

  protected Device remoteDevice;

  protected List<SyncEntity> entitiesToDeleteAfterTest = new ArrayList<>();


  @Before
  public void setUp() {
    entityManager = new EntityManagerStub();
    threadPool = new ThreadPool();

    localDevice = new Device("local");
    localDevice.setOsName("Linux");
    localDevice.setOsType(OsType.DESKTOP);
    localDevice.setOsVersion("4.9");

    remoteDevice = new Device("remote");
    remoteDevice.setName("Motorola Moto G4");
    remoteDevice.setOsName("Android");
    remoteDevice.setOsType(OsType.ANDROID);
    remoteDevice.setOsVersion("7.1");

    IPermissionsManager permissionsManager = new PermissionsManagerStub();

    underTest = createSyncModuleToTest(appContext, new Localization(), permissionsManager, threadPool);

    syncModuleConfiguration = new SyncModuleConfiguration(underTest.getSyncEntityTypeItCanHandle());
  }

  @After
  public void tearDown() {
    for(SyncEntity entityToDelete : entitiesToDeleteAfterTest) {
      underTest.synchronizedEntityRetrieved(createSyncJobItem(entityToDelete), SyncEntityState.DELETED);
    }
  }

  @NonNull
  protected SyncJobItem createSyncJobItem(SyncEntity entity) {
    return new SyncJobItem(syncModuleConfiguration, entity, localDevice, remoteDevice, getSyncEntityData(entity));
  }


  @NonNull
  protected abstract AndroidSyncModuleBase createSyncModuleToTest(Context context, Localization localization, IPermissionsManager permissionsManager, IThreadPool threadPool);

  @NonNull
  protected abstract SyncEntity createTestEntity();

  protected abstract void updateTestEntity(SyncEntity entityToUpdate);


  protected abstract void testReadEntity(SyncEntity entityToTest);

  protected abstract void testIfEntryHasSuccessfullyBeenAdded(SyncEntity entity);

  protected abstract void testIfEntryHasSuccessfullyBeenUpdated(SyncEntity entity);

  @NonNull
  protected abstract String getIdColumnForEntity();


  protected byte[] getSyncEntityData(SyncEntity entity) {
    return null; // may be overwritten in sub classes (for FileSyncEntities)
  }


  @Test
  public void readAllEntitiesAsync() {
    final List<SyncEntity> result = new ArrayList<>();
    final CountDownLatch countDownLatch = new CountDownLatch(1);

    underTest.readAllEntitiesAsync(new ReadEntitiesCallback() {
      @Override
      public void done(List<SyncEntity> entities) {
        result.addAll(entities);
        countDownLatch.countDown();
      }
    });

    try { countDownLatch.await(7, TimeUnit.SECONDS); } catch(Exception e) { }

    Assert.assertNotEquals(0, result.size());

    for(SyncEntity entity : result) {
      Assert.assertNotNull(entity.getLocalLookupKey());
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

    underTest.synchronizedEntityRetrieved(createSyncJobItem(entity), SyncEntityState.CREATED);

    testIfEntryHasSuccessfullyBeenAdded(entity);
  }


  @Test
  public void synchronizedUpdatedEntity_EntityGetsUpdated() throws ParseException {
    SyncEntity entity = createTestEntityAndAddToDeleteAfterTest();

    underTest.synchronizedEntityRetrieved(createSyncJobItem(entity), SyncEntityState.CREATED);

    updateTestEntity(entity);


    underTest.synchronizedEntityRetrieved(createSyncJobItem(entity), SyncEntityState.UPDATED);


    testIfEntryHasSuccessfullyBeenUpdated(entity);
  }


  @Test
  public void synchronizedDeletedEntity_EntityGetsRemoved() throws ParseException {
    SyncEntity entity = createTestEntityAndAddToDeleteAfterTest();

    underTest.synchronizedEntityRetrieved(createSyncJobItem(entity), SyncEntityState.CREATED);


    underTest.synchronizedEntityRetrieved(createSyncJobItem(entity), SyncEntityState.DELETED);


    testIfEntryHasSuccessfullyBeenRemoved(entity);
  }


  @Test
  public void addSyncEntityChangeListener_EntityGetsAdded_ListenerGetsCalled() {
    final List<SyncEntity> changedEntities = new ArrayList<>();
    final CountDownLatch countDownLatch = new CountDownLatch(1);

    underTest.addSyncEntityChangeListener(new SyncEntityChangeListener() {
      @Override
      public void entityChanged(SyncEntityChange syncEntityChange) {
        changedEntities.add(syncEntityChange.getSyncEntity());
        countDownLatch.countDown();
      }
    });

    SyncEntity syncEntity = createTestEntityAndAddToDeleteAfterTest();

    underTest.synchronizedEntityRetrieved(createSyncJobItem(syncEntity), SyncEntityState.CREATED);

    try { countDownLatch.await(3, TimeUnit.SECONDS); } catch(Exception ignored) { }

    Assert.assertTrue(changedEntities.size() > 0);
    // TODO: also check added Entity
  }

  @Test
  public void addSyncEntityChangeListener_EntityGetsUpdated_ListenerGetsCalled() {
    final List<SyncEntity> changedEntities = new ArrayList<>();
    final CountDownLatch countDownLatch = new CountDownLatch(1);

    SyncEntity syncEntity = createTestEntityAndAddToDeleteAfterTest();
    underTest.synchronizedEntityRetrieved(createSyncJobItem(syncEntity), SyncEntityState.CREATED);

    underTest.addSyncEntityChangeListener(new SyncEntityChangeListener() {
      @Override
      public void entityChanged(SyncEntityChange syncEntityChange) {
        changedEntities.add(syncEntityChange.getSyncEntity());
        countDownLatch.countDown();
      }
    });

    updateTestEntity(syncEntity);
    underTest.synchronizedEntityRetrieved(createSyncJobItem(syncEntity), SyncEntityState.UPDATED);

    try { countDownLatch.await(3, TimeUnit.SECONDS); } catch(Exception ignored) { }

    Assert.assertTrue(changedEntities.size() > 0);
    // TODO: also check added Entity
  }

  @Test
  public void addSyncEntityChangeListener_EntityGetsRemoved_ListenerGetsCalled() {
    final List<SyncEntity> changedEntities = new ArrayList<>();
    final CountDownLatch countDownLatch = new CountDownLatch(1);

    SyncEntity syncEntity = createTestEntityAndAddToDeleteAfterTest();
    underTest.synchronizedEntityRetrieved(createSyncJobItem(syncEntity), SyncEntityState.CREATED);

    underTest.addSyncEntityChangeListener(new SyncEntityChangeListener() {
      @Override
      public void entityChanged(SyncEntityChange syncEntityChange) {
        changedEntities.add(syncEntityChange.getSyncEntity());
        countDownLatch.countDown();
      }
    });

    underTest.synchronizedEntityRetrieved(createSyncJobItem(syncEntity), SyncEntityState.DELETED);

    try { countDownLatch.await(3, TimeUnit.SECONDS); } catch(Exception ignored) { }

    Assert.assertEquals(1, changedEntities.size());
    // TODO: also check added Entity
  }


  protected void testIfEntryHasSuccessfullyBeenRemoved(SyncEntity entity) {
    Assert.assertTrue(StringUtils.isNotNullOrEmpty(entity.getLocalLookupKey()));

    Cursor cursor = getCursorForEntity(entity);

    Assert.assertFalse(cursor.moveToFirst()); // assert entity does not exist anymore
  }


  protected Cursor getCursorForEntity(SyncEntity entity) {
    return appContext.getContentResolver().query(
        underTest.getContentUri(),
        null, // Which columns to return
        getIdColumnForEntity() + " = ?",       // Which rows to return (all rows)
        new String[] { entity.getLocalLookupKey() },       // Selection arguments (none)
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
