package net.dankito.sync.synchronization;


import net.dankito.sync.ContactSyncEntity;
import net.dankito.sync.Device;
import net.dankito.sync.LocalConfig;
import net.dankito.sync.PhoneNumberSyncEntity;
import net.dankito.sync.PhoneNumberType;
import net.dankito.sync.SyncConfiguration;
import net.dankito.sync.SyncEntity;
import net.dankito.sync.SyncEntityLocalLookupKeys;
import net.dankito.sync.SyncJobItem;
import net.dankito.sync.SyncModuleConfiguration;
import net.dankito.sync.data.IDataManager;
import net.dankito.sync.devices.DiscoveredDevice;
import net.dankito.sync.devices.IDevicesManager;
import net.dankito.sync.persistence.CouchbaseLiteEntityManagerBase;
import net.dankito.sync.persistence.CouchbaseLiteEntityManagerJava;
import net.dankito.sync.persistence.EntityManagerConfiguration;
import net.dankito.sync.persistence.IEntityManager;
import net.dankito.sync.synchronization.merge.IDataMerger;
import net.dankito.sync.synchronization.merge.JpaMetadataBasedDataMerger;
import net.dankito.sync.synchronization.modules.ISyncModule;
import net.dankito.sync.synchronization.util.SyncConfigurationManagerStub;
import net.dankito.sync.synchronization.util.SyncModuleMock;
import net.dankito.utils.ThreadPool;
import net.dankito.utils.services.JavaFileStorageService;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class SyncConfigurationManagerBaseTest {

  protected static final String TEST_CONTACT_SYNC_ENTITY_01_LOCAL_ID = "01";
  protected static final String TEST_CONTACT_SYNC_ENTITY_01_DISPLAY_NAME = "Mandela";
  protected static final String TEST_CONTACT_SYNC_ENTITY_01_PHONE_NUMBER_01 = "0123456789";
  protected static final String TEST_UPDATED_CONTACT_SYNC_ENTITY_01_PHONE_NUMBER_01 = "01234567890";
  protected static final PhoneNumberType TEST_CONTACT_SYNC_ENTITY_01_PHONE_NUMBER_TYPE_01 = PhoneNumberType.MOBILE;
  protected static final String TEST_CONTACT_SYNC_ENTITY_01_PHONE_NUMBER_02 = "03456789129";
  protected static final PhoneNumberType TEST_CONTACT_SYNC_ENTITY_01_PHONE_NUMBER_TYPE_02 = PhoneNumberType.HOME;
  protected static final String TEST_CONTACT_SYNC_ENTITY_01_PHONE_NUMBER_03 = "0789123456";
  protected static final PhoneNumberType TEST_CONTACT_SYNC_ENTITY_01_PHONE_NUMBER_TYPE_03 = PhoneNumberType.OTHER;

  protected static final String TEST_CONTACT_SYNC_ENTITY_02_LOCAL_ID = "02";
  protected static final String TEST_CONTACT_SYNC_ENTITY_02_DISPLAY_NAME = "Gandhi";
  protected static final String TEST_CONTACT_SYNC_ENTITY_02_PHONE_NUMBER_01 = "0987654321";
  protected static final PhoneNumberType TEST_CONTACT_SYNC_ENTITY_02_PHONE_NUMBER_TYPE_01 = PhoneNumberType.WORK;


  protected SyncConfigurationManagerBase underTest;


  protected ISyncManager syncManager;

  protected IEntityManager entityManager;

  protected IDevicesManager devicesManager;

  protected IDataManager dataManager;

  protected LocalConfig localConfig;

  protected Device remoteDevice;

  protected SyncConfiguration syncConfiguration;

  protected SyncModuleConfiguration syncModuleConfiguration;

  protected List<SyncEntity> entitiesToReturnFromReadAllEntitiesAsync = new ArrayList<>();

  protected SyncModuleMock syncModuleMock;


  @Before
  public void setUp() throws Exception {
    syncManager = Mockito.mock(ISyncManager.class);
    entityManager = new CouchbaseLiteEntityManagerJava(new EntityManagerConfiguration("testData", 1));
    devicesManager = Mockito.mock(IDevicesManager.class);
    IDataMerger dataMerger = new JpaMetadataBasedDataMerger((CouchbaseLiteEntityManagerBase)entityManager);

    Device localDevice = new Device("local");
    localConfig = new LocalConfig(localDevice);

    dataManager = Mockito.mock(IDataManager.class);
    Mockito.when(dataManager.getLocalConfig()).thenReturn(localConfig);

    remoteDevice = new Device("remote");
    DiscoveredDevice discoveredRemoteDevice = new DiscoveredDevice(remoteDevice, "1.1.1.1");

    syncModuleMock = new SyncModuleMock(entitiesToReturnFromReadAllEntitiesAsync);

    syncModuleConfiguration = new SyncModuleConfiguration(syncModuleMock.getSyncEntityTypeItCanHandle());
    syncConfiguration = new SyncConfiguration(localConfig.getLocalDevice(), remoteDevice,
        Arrays.asList(new SyncModuleConfiguration[] {  syncModuleConfiguration }));
    entityManager.persistEntity(syncModuleConfiguration);
    entityManager.persistEntity(syncConfiguration);

    localDevice.addSourceSyncConfiguration(syncConfiguration);
    remoteDevice.addDestinationSyncConfiguration(syncConfiguration);
    entityManager.persistEntity(localDevice);
    entityManager.persistEntity(remoteDevice);

    underTest = new SyncConfigurationManagerStub(syncManager, dataManager, entityManager, devicesManager, dataMerger, new JavaFileStorageService(), new ThreadPool(), discoveredRemoteDevice);


    SyncConfigurationManagerStub syncConfigurationManagerStub = (SyncConfigurationManagerStub)underTest;
    List<ISyncModule> mockedAvailableSyncModules = new ArrayList<>();
    mockedAvailableSyncModules.add(syncModuleMock);
    syncConfigurationManagerStub.setMockedAvailableSyncModules(mockedAvailableSyncModules);

    syncConfigurationManagerStub.startContinuousSynchronizationWithDevice(new DiscoveredDevice(remoteDevice, "1-1-Love"), syncConfiguration);
  }

  @After
  public void tearDown() {
    entityManager.close();

    new JavaFileStorageService().deleteFolderRecursively(entityManager.getDatabasePath());
  }


  @Test
  public void syncNewEntities() {
    List<SyncEntity> testEntities = new ArrayList<>();

    ContactSyncEntity testEntity01 = new ContactSyncEntity();
    testEntity01.setDisplayName(TEST_CONTACT_SYNC_ENTITY_01_DISPLAY_NAME);
    testEntity01.addPhoneNumber(createTestPhoneNumber("1.1", TEST_CONTACT_SYNC_ENTITY_01_PHONE_NUMBER_01, TEST_CONTACT_SYNC_ENTITY_01_PHONE_NUMBER_TYPE_01));
    testEntity01.addPhoneNumber(createTestPhoneNumber("1.2", TEST_CONTACT_SYNC_ENTITY_01_PHONE_NUMBER_02, TEST_CONTACT_SYNC_ENTITY_01_PHONE_NUMBER_TYPE_02));
    testEntity01.addPhoneNumber(createTestPhoneNumber("1.3", TEST_CONTACT_SYNC_ENTITY_01_PHONE_NUMBER_03, TEST_CONTACT_SYNC_ENTITY_01_PHONE_NUMBER_TYPE_03));
    testEntities.add(testEntity01);

    ContactSyncEntity testEntity02 = new ContactSyncEntity();
    testEntity02.setDisplayName(TEST_CONTACT_SYNC_ENTITY_02_DISPLAY_NAME);
    testEntity02.addPhoneNumber(createTestPhoneNumber("2", TEST_CONTACT_SYNC_ENTITY_02_PHONE_NUMBER_01, TEST_CONTACT_SYNC_ENTITY_02_PHONE_NUMBER_TYPE_01));
    testEntities.add(testEntity02);

    Assert.assertEquals(0, entityManager.getAllEntitiesOfType(ContactSyncEntity.class).size());
    Assert.assertEquals(0, entityManager.getAllEntitiesOfType(PhoneNumberSyncEntity.class).size());
    Assert.assertEquals(0, entityManager.getAllEntitiesOfType(SyncJobItem.class).size());
    Assert.assertEquals(0, entityManager.getAllEntitiesOfType(SyncEntityLocalLookupKeys.class).size());


    mockSynchronizeEntitiesWithDevice(testEntities);


    Assert.assertEquals(2, entityManager.getAllEntitiesOfType(ContactSyncEntity.class).size());
    Assert.assertEquals(4, entityManager.getAllEntitiesOfType(PhoneNumberSyncEntity.class).size());
    Assert.assertEquals(2, entityManager.getAllEntitiesOfType(SyncJobItem.class).size());
    Assert.assertEquals(6, entityManager.getAllEntitiesOfType(SyncEntityLocalLookupKeys.class).size());

    List<ContactSyncEntity> contacts = entityManager.getAllEntitiesOfType(ContactSyncEntity.class);
    for(ContactSyncEntity contact : contacts) {
      if(TEST_CONTACT_SYNC_ENTITY_01_DISPLAY_NAME.equals(contact.getDisplayName())) {
        Assert.assertEquals(3, contact.getPhoneNumbers().size());
      }
      else if(TEST_CONTACT_SYNC_ENTITY_02_DISPLAY_NAME.equals(contact.getDisplayName())) {
        Assert.assertEquals(1, contact.getPhoneNumbers().size());
      }
    }
  }

  @Test
  public void syncPersistedButUnsyncedEntities() {
    List<SyncEntity> testEntities = new ArrayList<>();
    SyncEntity testEntity01 = new ContactSyncEntity();
    SyncEntity testEntity02 = new ContactSyncEntity();
    testEntities.add(testEntity01);
    testEntities.add(testEntity02);
    entityManager.persistEntity(testEntity01);

    Assert.assertEquals(1, entityManager.getAllEntitiesOfType(ContactSyncEntity.class).size());
    Assert.assertEquals(0, entityManager.getAllEntitiesOfType(SyncJobItem.class).size());
    Assert.assertEquals(0, entityManager.getAllEntitiesOfType(SyncEntityLocalLookupKeys.class).size());


    mockSynchronizeEntitiesWithDevice(testEntities);


    Assert.assertEquals(2, entityManager.getAllEntitiesOfType(ContactSyncEntity.class).size());
    Assert.assertEquals(2, entityManager.getAllEntitiesOfType(SyncJobItem.class).size());
    Assert.assertEquals(2, entityManager.getAllEntitiesOfType(SyncEntityLocalLookupKeys.class).size());
  }


  @Test
  public void syncUpdatedEntities() {
    List<SyncEntity> testEntities = new ArrayList<>();
    ContactSyncEntity testEntity01 = new ContactSyncEntity();
    testEntity01.setLocalLookupKey(TEST_CONTACT_SYNC_ENTITY_01_LOCAL_ID);
    testEntities.add(testEntity01);
    ContactSyncEntity testEntity02 = new ContactSyncEntity();
    testEntity02.setLocalLookupKey(TEST_CONTACT_SYNC_ENTITY_02_LOCAL_ID);
    testEntities.add(testEntity02);

    mockSynchronizeEntitiesWithDevice(testEntities);

    Assert.assertEquals(2, entityManager.getAllEntitiesOfType(ContactSyncEntity.class).size());
    Assert.assertEquals(0, entityManager.getAllEntitiesOfType(PhoneNumberSyncEntity.class).size());
    Assert.assertEquals(2, entityManager.getAllEntitiesOfType(SyncJobItem.class).size());
    Assert.assertEquals(2, entityManager.getAllEntitiesOfType(SyncEntityLocalLookupKeys.class).size());


    testEntities.clear();

    ContactSyncEntity updatedTestEntity01 = new ContactSyncEntity();
    updatedTestEntity01.setLocalLookupKey(TEST_CONTACT_SYNC_ENTITY_01_LOCAL_ID);
    updatedTestEntity01.setDisplayName(TEST_CONTACT_SYNC_ENTITY_01_DISPLAY_NAME);
    updatedTestEntity01.addPhoneNumber(createTestPhoneNumber("1.2", TEST_CONTACT_SYNC_ENTITY_01_PHONE_NUMBER_02, TEST_CONTACT_SYNC_ENTITY_01_PHONE_NUMBER_TYPE_02));
    updatedTestEntity01.addPhoneNumber(createTestPhoneNumber("1.3", TEST_CONTACT_SYNC_ENTITY_01_PHONE_NUMBER_03, TEST_CONTACT_SYNC_ENTITY_01_PHONE_NUMBER_TYPE_03));
    updatedTestEntity01.setLastModifiedOnDevice(new Date());
    testEntities.add(updatedTestEntity01);

    ContactSyncEntity updatedTestEntity02 = new ContactSyncEntity();
    updatedTestEntity02.setLocalLookupKey(TEST_CONTACT_SYNC_ENTITY_02_LOCAL_ID);
    updatedTestEntity02.setDisplayName(TEST_CONTACT_SYNC_ENTITY_02_DISPLAY_NAME);
    updatedTestEntity02.addPhoneNumber(createTestPhoneNumber("2", TEST_CONTACT_SYNC_ENTITY_02_PHONE_NUMBER_01, TEST_CONTACT_SYNC_ENTITY_02_PHONE_NUMBER_TYPE_01));
    updatedTestEntity02.setLastModifiedOnDevice(new Date());
    testEntities.add(updatedTestEntity02);


    mockSynchronizeEntitiesWithDevice(testEntities);


    Assert.assertEquals(2, entityManager.getAllEntitiesOfType(ContactSyncEntity.class).size());
    Assert.assertEquals(3, entityManager.getAllEntitiesOfType(PhoneNumberSyncEntity.class).size());
    Assert.assertEquals(4, entityManager.getAllEntitiesOfType(SyncJobItem.class).size());
    Assert.assertEquals(5, entityManager.getAllEntitiesOfType(SyncEntityLocalLookupKeys.class).size());

    List<ContactSyncEntity> contacts = entityManager.getAllEntitiesOfType(ContactSyncEntity.class);

    for(ContactSyncEntity retrievedContact : contacts) {
      Assert.assertNotEquals(0, retrievedContact.getPhoneNumbers().size());
      PhoneNumberSyncEntity retrievedPhoneNumber = retrievedContact.getPhoneNumbers().get(0);

      if(TEST_CONTACT_SYNC_ENTITY_01_DISPLAY_NAME.equals(retrievedContact.getDisplayName())) {
        testUpdatedPhoneNumberOfFirstContact(retrievedPhoneNumber);

        PhoneNumberSyncEntity secondRetrievedPhoneNumber = retrievedContact.getPhoneNumbers().get(1);
        testUpdatedPhoneNumberOfFirstContact(secondRetrievedPhoneNumber);
      }
      else {
        Assert.assertEquals(TEST_CONTACT_SYNC_ENTITY_02_PHONE_NUMBER_01, retrievedPhoneNumber.getNumber());
        Assert.assertEquals(TEST_CONTACT_SYNC_ENTITY_02_PHONE_NUMBER_TYPE_01, retrievedPhoneNumber.getType());
      }
    }
  }

  protected void testUpdatedPhoneNumberOfFirstContact(PhoneNumberSyncEntity retrievedPhoneNumber) {
    if(TEST_CONTACT_SYNC_ENTITY_01_PHONE_NUMBER_02.equals(retrievedPhoneNumber.getNumber())) {
      Assert.assertEquals(TEST_CONTACT_SYNC_ENTITY_01_PHONE_NUMBER_02, retrievedPhoneNumber.getNumber());
      Assert.assertEquals(TEST_CONTACT_SYNC_ENTITY_01_PHONE_NUMBER_TYPE_02, retrievedPhoneNumber.getType());
    }
    else {
      Assert.assertEquals(TEST_CONTACT_SYNC_ENTITY_01_PHONE_NUMBER_03, retrievedPhoneNumber.getNumber());
      Assert.assertEquals(TEST_CONTACT_SYNC_ENTITY_01_PHONE_NUMBER_TYPE_03, retrievedPhoneNumber.getType());
    }
  }

  // only a SyncEntity Property has been updated - check if this change is detected and synchronized
  @Test
  public void syncUpdatedProperty() {
    List<SyncEntity> testEntities = new ArrayList<>();
    ContactSyncEntity testEntity01 = new ContactSyncEntity();
    testEntity01.setLocalLookupKey(TEST_CONTACT_SYNC_ENTITY_01_LOCAL_ID);
    testEntity01.setDisplayName(TEST_CONTACT_SYNC_ENTITY_01_DISPLAY_NAME);
    testEntity01.addPhoneNumber(createTestPhoneNumber("1.1", TEST_CONTACT_SYNC_ENTITY_01_PHONE_NUMBER_01, TEST_CONTACT_SYNC_ENTITY_01_PHONE_NUMBER_TYPE_01));
    testEntities.add(testEntity01);

    mockSynchronizeEntitiesWithDevice(testEntities);

    Assert.assertEquals(1, entityManager.getAllEntitiesOfType(ContactSyncEntity.class).size());
    Assert.assertEquals(1, entityManager.getAllEntitiesOfType(PhoneNumberSyncEntity.class).size());
    Assert.assertEquals(1, entityManager.getAllEntitiesOfType(SyncJobItem.class).size());
    Assert.assertEquals(2, entityManager.getAllEntitiesOfType(SyncEntityLocalLookupKeys.class).size());


    testEntities.clear();

    ContactSyncEntity updatedTestEntity01 = new ContactSyncEntity();
    updatedTestEntity01.setLocalLookupKey(TEST_CONTACT_SYNC_ENTITY_01_LOCAL_ID);
    updatedTestEntity01.setDisplayName(TEST_CONTACT_SYNC_ENTITY_01_DISPLAY_NAME);
    updatedTestEntity01.addPhoneNumber(createTestPhoneNumber("1.1", TEST_UPDATED_CONTACT_SYNC_ENTITY_01_PHONE_NUMBER_01, TEST_CONTACT_SYNC_ENTITY_01_PHONE_NUMBER_TYPE_01));
    testEntities.add(updatedTestEntity01);


    mockSynchronizeEntitiesWithDevice(testEntities);


    Assert.assertEquals(1, entityManager.getAllEntitiesOfType(ContactSyncEntity.class).size());
    Assert.assertEquals(1, entityManager.getAllEntitiesOfType(PhoneNumberSyncEntity.class).size());
    Assert.assertEquals(2, entityManager.getAllEntitiesOfType(SyncJobItem.class).size());
    Assert.assertEquals(2, entityManager.getAllEntitiesOfType(SyncEntityLocalLookupKeys.class).size());

    ContactSyncEntity updatedContact = entityManager.getAllEntitiesOfType(ContactSyncEntity.class).get(0);

    Assert.assertEquals(TEST_UPDATED_CONTACT_SYNC_ENTITY_01_PHONE_NUMBER_01, updatedContact.getPhoneNumbers().get(0).getNumber());
  }

  @Test
  public void syncUneditedEntitiesToSync_NothingGetsSynced() {
    List<SyncEntity> testEntities = new ArrayList<>();
    ContactSyncEntity testEntity01 = new ContactSyncEntity();
    testEntity01.setLocalLookupKey(TEST_CONTACT_SYNC_ENTITY_01_LOCAL_ID);
    testEntities.add(testEntity01);
    ContactSyncEntity testEntity02 = new ContactSyncEntity();
    testEntity02.setLocalLookupKey(TEST_CONTACT_SYNC_ENTITY_02_LOCAL_ID);
    testEntities.add(testEntity02);

    mockSynchronizeEntitiesWithDevice(testEntities);

    Assert.assertEquals(2, entityManager.getAllEntitiesOfType(ContactSyncEntity.class).size());
    Assert.assertEquals(2, entityManager.getAllEntitiesOfType(SyncJobItem.class).size());
    Assert.assertEquals(2, entityManager.getAllEntitiesOfType(SyncEntityLocalLookupKeys.class).size());


    testEntity01.setDisplayName(TEST_CONTACT_SYNC_ENTITY_01_DISPLAY_NAME);
    testEntity02.setDisplayName(TEST_CONTACT_SYNC_ENTITY_02_DISPLAY_NAME);


    mockSynchronizeEntitiesWithDevice(testEntities);


    Assert.assertEquals(2, entityManager.getAllEntitiesOfType(ContactSyncEntity.class).size());
    Assert.assertEquals(2, entityManager.getAllEntitiesOfType(SyncJobItem.class).size());
    Assert.assertEquals(2, entityManager.getAllEntitiesOfType(SyncEntityLocalLookupKeys.class).size());
  }


  @Test
  public void deleteEntities() {
    List<SyncEntity> testEntities = new ArrayList<>();
    ContactSyncEntity testEntity01 = new ContactSyncEntity();
    testEntity01.setLocalLookupKey(TEST_CONTACT_SYNC_ENTITY_01_LOCAL_ID);
    testEntities.add(testEntity01);
    ContactSyncEntity testEntity02 = new ContactSyncEntity();
    testEntity02.setLocalLookupKey(TEST_CONTACT_SYNC_ENTITY_02_LOCAL_ID);
    testEntities.add(testEntity02);

    mockSynchronizeEntitiesWithDevice(testEntities);

    Assert.assertEquals(2, entityManager.getAllEntitiesOfType(ContactSyncEntity.class).size());
    Assert.assertEquals(2, entityManager.getAllEntitiesOfType(SyncJobItem.class).size());
    Assert.assertEquals(2, entityManager.getAllEntitiesOfType(SyncEntityLocalLookupKeys.class).size());


    testEntities.clear();
    entityManager.deleteEntity(testEntity01);
    entityManager.deleteEntity(testEntity02);


    mockSynchronizeEntitiesWithDevice(testEntities);


    Assert.assertEquals(0, entityManager.getAllEntitiesOfType(ContactSyncEntity.class).size());
    Assert.assertEquals(2, entityManager.getAllEntitiesOfType(SyncJobItem.class).size());
    Assert.assertEquals(0, entityManager.getAllEntitiesOfType(SyncEntityLocalLookupKeys.class).size());
  }



  protected PhoneNumberSyncEntity createTestPhoneNumber(String lookupKey, String phoneNumber, PhoneNumberType phoneNumberType) {
    PhoneNumberSyncEntity entity = new PhoneNumberSyncEntity(phoneNumber, phoneNumberType);

    entity.setLocalLookupKey(lookupKey);

    return entity;
  }


  protected void mockSynchronizeEntitiesWithDevice(final List<SyncEntity> testEntities) {
    syncModuleMock.callEntityChangedListeners(testEntities);
  }

}
