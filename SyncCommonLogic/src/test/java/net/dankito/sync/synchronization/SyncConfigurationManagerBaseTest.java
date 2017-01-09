package net.dankito.sync.synchronization;


import net.dankito.sync.ContactSyncEntity;
import net.dankito.sync.Device;
import net.dankito.sync.ISyncModule;
import net.dankito.sync.LocalConfig;
import net.dankito.sync.ReadEntitiesCallback;
import net.dankito.sync.SyncConfiguration;
import net.dankito.sync.SyncEntity;
import net.dankito.sync.SyncEntityLocalLookUpKeys;
import net.dankito.sync.SyncJobItem;
import net.dankito.sync.SyncModuleConfiguration;
import net.dankito.sync.devices.DiscoveredDevice;
import net.dankito.sync.devices.IDevicesManager;
import net.dankito.sync.persistence.CouchbaseLiteEntityManagerJava;
import net.dankito.sync.persistence.EntityManagerConfiguration;
import net.dankito.sync.persistence.IEntityManager;
import net.dankito.sync.synchronization.util.SyncConfigurationManagerStub;
import net.dankito.utils.services.JavaFileStorageService;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SyncConfigurationManagerBaseTest {

  protected static final String TEST_SYNC_MODULE_NAME = "TestSyncModule";

  protected static final String TEST_CONTACT_SYNC_ENTITY_01_LOCAL_ID = "01";
  protected static final String TEST_CONTACT_SYNC_ENTITY_01_DISPLAY_NAME = "Mandela";

  protected static final String TEST_CONTACT_SYNC_ENTITY_02_LOCAL_ID = "02";
  protected static final String TEST_CONTACT_SYNC_ENTITY_02_DISPLAY_NAME = "Gandhi";


  protected SyncConfigurationManagerBase underTest;


  protected ISyncManager syncManager;

  protected IEntityManager entityManager;

  protected IDevicesManager devicesManager;

  protected LocalConfig localConfig;

  protected Device remoteDevice;

  protected SyncConfiguration syncConfiguration;

  protected SyncModuleConfiguration syncModuleConfiguration;


  @Before
  public void setUp() throws Exception {
    syncManager = Mockito.mock(ISyncManager.class);
    entityManager = new CouchbaseLiteEntityManagerJava(new EntityManagerConfiguration("testData", 1));
    devicesManager = Mockito.mock(IDevicesManager.class);

    Device localDevice = new Device("local");
    localConfig = new LocalConfig(localDevice);

    remoteDevice = new Device("remote");

    syncModuleConfiguration = new SyncModuleConfiguration(TEST_SYNC_MODULE_NAME);
    syncConfiguration = new SyncConfiguration(localConfig.getLocalDevice(), remoteDevice,
        Arrays.asList(new SyncModuleConfiguration[] {  syncModuleConfiguration }));
    entityManager.persistEntity(syncModuleConfiguration);
    entityManager.persistEntity(syncConfiguration);

    underTest = new SyncConfigurationManagerStub(syncManager, entityManager, devicesManager, localConfig);
  }

  @After
  public void tearDown() {
    entityManager.close();

    new JavaFileStorageService().deleteFolderRecursively(entityManager.getDatabasePath());
  }


  @Test
  public void syncNewEntities() {
    List<SyncEntity> testEntities = new ArrayList<>();
    SyncEntity testEntity01 = new ContactSyncEntity(null);
    SyncEntity testEntity02 = new ContactSyncEntity(null);
    testEntities.add(testEntity01);
    testEntities.add(testEntity02);

    Assert.assertEquals(0, entityManager.getAllEntitiesOfType(ContactSyncEntity.class).size());
    Assert.assertEquals(0, entityManager.getAllEntitiesOfType(SyncJobItem.class).size());
    Assert.assertEquals(0, entityManager.getAllEntitiesOfType(SyncEntityLocalLookUpKeys.class).size());


    mockSynchronizeEntitiesWithDevice(testEntities);


    Assert.assertEquals(2, entityManager.getAllEntitiesOfType(ContactSyncEntity.class).size());
    Assert.assertEquals(2, entityManager.getAllEntitiesOfType(SyncJobItem.class).size());
    Assert.assertEquals(2, entityManager.getAllEntitiesOfType(SyncEntityLocalLookUpKeys.class).size());
  }

  @Test
  public void syncPersistedButUnsyncedEntities() {
    List<SyncEntity> testEntities = new ArrayList<>();
    SyncEntity testEntity01 = new ContactSyncEntity(null);
    SyncEntity testEntity02 = new ContactSyncEntity(null);
    testEntities.add(testEntity01);
    testEntities.add(testEntity02);
    entityManager.persistEntity(testEntity01);

    Assert.assertEquals(1, entityManager.getAllEntitiesOfType(ContactSyncEntity.class).size());
    Assert.assertEquals(0, entityManager.getAllEntitiesOfType(SyncJobItem.class).size());
    Assert.assertEquals(0, entityManager.getAllEntitiesOfType(SyncEntityLocalLookUpKeys.class).size());


    mockSynchronizeEntitiesWithDevice(testEntities);


    Assert.assertEquals(2, entityManager.getAllEntitiesOfType(ContactSyncEntity.class).size());
    Assert.assertEquals(2, entityManager.getAllEntitiesOfType(SyncJobItem.class).size());
    Assert.assertEquals(2, entityManager.getAllEntitiesOfType(SyncEntityLocalLookUpKeys.class).size());
  }


  @Test
  public void syncUpdatedEntities() {
    List<SyncEntity> testEntities = new ArrayList<>();
    ContactSyncEntity testEntity01 = new ContactSyncEntity(null);
    testEntity01.setLookUpKeyOnSourceDevice(TEST_CONTACT_SYNC_ENTITY_01_LOCAL_ID);
    testEntities.add(testEntity01);
    ContactSyncEntity testEntity02 = new ContactSyncEntity(null);
    testEntity02.setLookUpKeyOnSourceDevice(TEST_CONTACT_SYNC_ENTITY_02_LOCAL_ID);
    testEntities.add(testEntity02);

    mockSynchronizeEntitiesWithDevice(testEntities);

    Assert.assertEquals(2, entityManager.getAllEntitiesOfType(ContactSyncEntity.class).size());
    Assert.assertEquals(2, entityManager.getAllEntitiesOfType(SyncJobItem.class).size());
    Assert.assertEquals(2, entityManager.getAllEntitiesOfType(SyncEntityLocalLookUpKeys.class).size());


    testEntity01.setDisplayName(TEST_CONTACT_SYNC_ENTITY_01_DISPLAY_NAME);
    testEntity02.setDisplayName(TEST_CONTACT_SYNC_ENTITY_02_DISPLAY_NAME);


    mockSynchronizeEntitiesWithDevice(testEntities);


    Assert.assertEquals(2, entityManager.getAllEntitiesOfType(ContactSyncEntity.class).size());
    Assert.assertEquals(4, entityManager.getAllEntitiesOfType(SyncJobItem.class).size());
    Assert.assertEquals(2, entityManager.getAllEntitiesOfType(SyncEntityLocalLookUpKeys.class).size());
  }

  @Test
  public void syncUneditedEntitiesToSync_NothingGetsSynced() {
    List<SyncEntity> testEntities = new ArrayList<>();
    ContactSyncEntity testEntity01 = new ContactSyncEntity(null);
    testEntity01.setLookUpKeyOnSourceDevice(TEST_CONTACT_SYNC_ENTITY_01_LOCAL_ID);
    testEntities.add(testEntity01);
    ContactSyncEntity testEntity02 = new ContactSyncEntity(null);
    testEntity02.setLookUpKeyOnSourceDevice(TEST_CONTACT_SYNC_ENTITY_02_LOCAL_ID);
    testEntities.add(testEntity02);

    mockSynchronizeEntitiesWithDevice(testEntities);

    Assert.assertEquals(2, entityManager.getAllEntitiesOfType(ContactSyncEntity.class).size());
    Assert.assertEquals(2, entityManager.getAllEntitiesOfType(SyncJobItem.class).size());
    Assert.assertEquals(2, entityManager.getAllEntitiesOfType(SyncEntityLocalLookUpKeys.class).size());


    testEntity01.setDisplayName(TEST_CONTACT_SYNC_ENTITY_01_DISPLAY_NAME);
    testEntity02.setDisplayName(TEST_CONTACT_SYNC_ENTITY_02_DISPLAY_NAME);


    mockSynchronizeEntitiesWithDevice(testEntities);


    Assert.assertEquals(2, entityManager.getAllEntitiesOfType(ContactSyncEntity.class).size());
    Assert.assertEquals(2, entityManager.getAllEntitiesOfType(SyncJobItem.class).size());
    Assert.assertEquals(2, entityManager.getAllEntitiesOfType(SyncEntityLocalLookUpKeys.class).size());
  }


  @Test
  public void deleteEntities() {
    List<SyncEntity> testEntities = new ArrayList<>();
    ContactSyncEntity testEntity01 = new ContactSyncEntity(null);
    testEntity01.setLookUpKeyOnSourceDevice(TEST_CONTACT_SYNC_ENTITY_01_LOCAL_ID);
    testEntities.add(testEntity01);
    ContactSyncEntity testEntity02 = new ContactSyncEntity(null);
    testEntity02.setLookUpKeyOnSourceDevice(TEST_CONTACT_SYNC_ENTITY_02_LOCAL_ID);
    testEntities.add(testEntity02);

    mockSynchronizeEntitiesWithDevice(testEntities);

    Assert.assertEquals(2, entityManager.getAllEntitiesOfType(ContactSyncEntity.class).size());
    Assert.assertEquals(2, entityManager.getAllEntitiesOfType(SyncJobItem.class).size());
    Assert.assertEquals(2, entityManager.getAllEntitiesOfType(SyncEntityLocalLookUpKeys.class).size());


    testEntities.clear();
    entityManager.deleteEntity(testEntity01);
    entityManager.deleteEntity(testEntity02);


    mockSynchronizeEntitiesWithDevice(testEntities);


    Assert.assertEquals(0, entityManager.getAllEntitiesOfType(ContactSyncEntity.class).size());
    Assert.assertEquals(2, entityManager.getAllEntitiesOfType(SyncJobItem.class).size());
    Assert.assertEquals(0, entityManager.getAllEntitiesOfType(SyncEntityLocalLookUpKeys.class).size());
  }


  protected void mockSynchronizeEntitiesWithDevice(final List<SyncEntity> testEntities) {
    ISyncModule testSyncModule = new ISyncModule() {
      @Override
      public void readAllEntitiesAsync(SyncModuleConfiguration syncModuleConfiguration, ReadEntitiesCallback callback) {
        callback.done(testEntities);
      }
    };

    SyncConfigurationManagerStub syncConfigurationManagerStub = (SyncConfigurationManagerStub)underTest;
    Map<String, ISyncModule> mockedAvailableSyncModules = new HashMap<>();
    mockedAvailableSyncModules.put(TEST_SYNC_MODULE_NAME, testSyncModule);
    syncConfigurationManagerStub.setMockedAvailableSyncModules(mockedAvailableSyncModules);

    syncConfigurationManagerStub.startContinuouslySynchronizationWithDevice(new DiscoveredDevice(remoteDevice, "1-1-Love"), syncConfiguration);
  }
}
