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
import net.dankito.utils.ObjectHolder;
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

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

/**
 * Created by ganymed on 05/01/17.
 */

@RunWith(AndroidJUnit4.class)
public abstract class AndroidSyncModuleTestBase {

  protected static final String SYNC_JOB_ITEM_ID_START = "SyncJobItem_";

  protected static int nextSyncJobItemId = 0;


  protected AndroidSyncModuleBase underTest;


  protected SyncModuleConfiguration syncModuleConfiguration;

  protected Context appContext = InstrumentationRegistry.getTargetContext();

  protected IEntityManager entityManager;

  protected IThreadPool threadPool;

  protected Device localDevice;

  protected Device remoteDevice;

  protected List<Cursor> cursorsToCloseAfterTest = new ArrayList<>();

  protected List<SyncEntity> entitiesToDeleteAfterTest = new ArrayList<>();


  @Before
  public void setUp() throws NoSuchFieldException {
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

    underTest = createSyncModuleToTest(appContext, new Localization(), threadPool);

    syncModuleConfiguration = new SyncModuleConfiguration(underTest.getSyncEntityTypeItCanHandle());
    syncModuleConfiguration.setSourcePath("");
    syncModuleConfiguration.setDestinationPath("");
  }

  @After
  public void tearDown() {
    for(SyncEntity entityToDelete : entitiesToDeleteAfterTest) {
      underTest.handleRetrievedSynchronizedEntityAsync(createSyncJobItem(entityToDelete), SyncEntityState.DELETED, getNoOpCallback());
    }

    for(Cursor cursor : cursorsToCloseAfterTest) {
      cursor.close();
    }
  }


  @NonNull
  protected SyncJobItem createSyncJobItem(SyncEntity entity) {
    return new SyncJobItem(syncModuleConfiguration, entity, localDevice, remoteDevice) {
      @Override
      public String getId() {
        return SYNC_JOB_ITEM_ID_START + ++nextSyncJobItemId;
      }
    };
  }


  @NonNull
  protected abstract AndroidSyncModuleBase createSyncModuleToTest(Context context, Localization localization, IThreadPool threadPool);

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
    final ObjectHolder<List<? extends SyncEntity>> result = new ObjectHolder<>();
    final CountDownLatch countDownLatch = new CountDownLatch(1);

    underTest.readAllEntitiesAsync(new ReadEntitiesCallback() {
      @Override
      public void done(boolean wasSuccessful, List<? extends SyncEntity> entities) {
        result.setObject(entities);
        countDownLatch.countDown();
      }
    });

    try { countDownLatch.await(30, TimeUnit.SECONDS); } catch(Exception e) { }

    assertThat(result.isObjectSet(), is(true));
    assertThat(result.getObject().size(), is(not(0)));

    for(SyncEntity entity : result.getObject()) {
      assertThat(entity.getLocalLookupKey(), notNullValue());
      assertThat(entity.getLastModifiedOnDevice(), notNullValue());

      if(entity instanceof ContactSyncEntity == false) { // is null for ContactSyncEntities
        assertThat(entity.getCreatedOnDevice(), notNullValue());
      }

      testReadEntity(entity);
    }
  }


  @Test
  public void synchronizedNewEntity_EntityGetsAdded() throws ParseException {
    SyncEntity entity = createTestEntityAndAddToDeleteAfterTest();

    CountDownLatch countDownLatch = new CountDownLatch(1);

    underTest.handleRetrievedSynchronizedEntityAsync(createSyncJobItem(entity), SyncEntityState.CREATED, getCountDownCallback(countDownLatch));

    try { countDownLatch.await(3, TimeUnit.SECONDS); } catch(Exception ignored) { }

    testIfEntryHasSuccessfullyBeenAdded(entity);
  }


  @Test
  public void synchronizedUpdatedEntity_EntityGetsUpdated() throws ParseException {
    SyncEntity entity = createTestEntityAndAddToDeleteAfterTest();

    CountDownLatch countDownLatch = new CountDownLatch(2);

    underTest.handleRetrievedSynchronizedEntityAsync(createSyncJobItem(entity), SyncEntityState.CREATED, getCountDownCallback(countDownLatch));

    updateTestEntity(entity);


    underTest.handleRetrievedSynchronizedEntityAsync(createSyncJobItem(entity), SyncEntityState.CHANGED, getCountDownCallback(countDownLatch));


    testIfEntryHasSuccessfullyBeenUpdated(entity);
  }


  @Test
  public void synchronizedDeletedEntity_EntityGetsRemoved() throws ParseException {
    SyncEntity entity = createTestEntityAndAddToDeleteAfterTest();

    final CountDownLatch countDownLatch = new CountDownLatch(2);

    underTest.handleRetrievedSynchronizedEntityAsync(createSyncJobItem(entity), SyncEntityState.CREATED, getCountDownCallback(countDownLatch));


    underTest.handleRetrievedSynchronizedEntityAsync(createSyncJobItem(entity), SyncEntityState.DELETED, getCountDownCallback(countDownLatch));


    testIfEntryHasSuccessfullyBeenRemoved(entity);
  }


  @Test
  public void addSyncEntityChangeListener_EntityGetsAdded_ListenerGetsCalled() {
    final List<SyncEntity> changedEntities = new ArrayList<>();
    final CountDownLatch countDownLatch = new CountDownLatch(2);

    underTest.addSyncEntityChangeListener(new SyncEntityChangeListener() {
      @Override
      public void entityChanged(SyncEntityChange syncEntityChange) {
        changedEntities.add(syncEntityChange.getSyncEntity());
        countDownLatch.countDown();
      }
    });

    SyncEntity syncEntity = createTestEntityAndAddToDeleteAfterTest();

    underTest.handleRetrievedSynchronizedEntityAsync(createSyncJobItem(syncEntity), SyncEntityState.CREATED, getCountDownCallback(countDownLatch));

    try { countDownLatch.await(3, TimeUnit.SECONDS); } catch(Exception ignored) { }

    Assert.assertTrue(changedEntities.size() > 0);
    // TODO: also check added Entity
  }

  @Test
  public void addSyncEntityChangeListener_EntityGetsUpdated_ListenerGetsCalled() {
    final List<SyncEntity> changedEntities = new ArrayList<>();
    final CountDownLatch countDownLatch = new CountDownLatch(3);

    SyncEntity syncEntity = createTestEntityAndAddToDeleteAfterTest();
    underTest.handleRetrievedSynchronizedEntityAsync(createSyncJobItem(syncEntity), SyncEntityState.CREATED, getCountDownCallback(countDownLatch));

    underTest.addSyncEntityChangeListener(new SyncEntityChangeListener() {
      @Override
      public void entityChanged(SyncEntityChange syncEntityChange) {
        changedEntities.add(syncEntityChange.getSyncEntity());
        countDownLatch.countDown();
      }
    });

    updateTestEntity(syncEntity);
    underTest.handleRetrievedSynchronizedEntityAsync(createSyncJobItem(syncEntity), SyncEntityState.CHANGED, getCountDownCallback(countDownLatch));

    try { countDownLatch.await(3, TimeUnit.SECONDS); } catch(Exception ignored) { }

    Assert.assertTrue(changedEntities.size() > 0);
    // TODO: also check added Entity
  }

  @Test
  public void addSyncEntityChangeListener_EntityGetsRemoved_ListenerGetsCalled() {
    final List<SyncEntity> changedEntities = new ArrayList<>();
    final CountDownLatch countDownLatch = new CountDownLatch(3);

    SyncEntity syncEntity = createTestEntityAndAddToDeleteAfterTest();
    underTest.handleRetrievedSynchronizedEntityAsync(createSyncJobItem(syncEntity), SyncEntityState.CREATED, getCountDownCallback(countDownLatch));

    underTest.addSyncEntityChangeListener(new SyncEntityChangeListener() {
      @Override
      public void entityChanged(SyncEntityChange syncEntityChange) {
        changedEntities.add(syncEntityChange.getSyncEntity());
        countDownLatch.countDown();
      }
    });

    underTest.handleRetrievedSynchronizedEntityAsync(createSyncJobItem(syncEntity), SyncEntityState.DELETED, getCountDownCallback(countDownLatch));

    try { countDownLatch.await(3, TimeUnit.SECONDS); } catch(Exception ignored) { }

    Assert.assertEquals(1, changedEntities.size());
    // TODO: also check added Entity
  }


  protected void testIfEntryHasSuccessfullyBeenRemoved(SyncEntity entity) {
    Assert.assertTrue(StringUtils.isNotNullOrEmpty(entity.getLocalLookupKey()));

    Cursor cursor = getCursorForEntity(entity);

    Assert.assertFalse(cursor.moveToFirst()); // assert entity does not exist anymore

    cursor.close();
  }


  protected Cursor getCursorForEntity(SyncEntity entity) {
    Cursor cursor = appContext.getContentResolver().query(
        underTest.getContentUri(),
        null, // Which columns to return
        getIdColumnForEntity() + " = ?",       // Which rows to return (all rows)
        new String[] { entity.getLocalLookupKey() },       // Selection arguments (none)
        null        // Ordering
    );

    cursorsToCloseAfterTest.add(cursor);

    return cursor;
  }


  protected SyncEntity createTestEntityAndAddToDeleteAfterTest() {
    SyncEntity testEntity = createTestEntity();

    addEntityToDeleteAfterTest(testEntity);

    return testEntity;
  }

  protected void addEntityToDeleteAfterTest(SyncEntity entity) {
    entitiesToDeleteAfterTest.add(entity);
  }


  protected HandleRetrievedSynchronizedEntityCallback getCountDownCallback(final CountDownLatch countDownLatch) {
    return new HandleRetrievedSynchronizedEntityCallback() {
      @Override
      public void done(HandleRetrievedSynchronizedEntityResult result) {
        countDownLatch.countDown();
      }
    };
  }

  protected HandleRetrievedSynchronizedEntityCallback getNoOpCallback() {
    return new HandleRetrievedSynchronizedEntityCallback() {
      @Override
      public void done(HandleRetrievedSynchronizedEntityResult result) {

      }
    };
  }

}
