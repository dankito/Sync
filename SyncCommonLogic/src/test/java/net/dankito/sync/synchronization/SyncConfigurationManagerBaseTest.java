package net.dankito.sync.synchronization;


import net.dankito.sync.BaseEntity;
import net.dankito.sync.ContactSyncEntity;
import net.dankito.sync.Device;
import net.dankito.sync.EmailSyncEntity;
import net.dankito.sync.EmailType;
import net.dankito.sync.FileSyncEntity;
import net.dankito.sync.LocalConfig;
import net.dankito.sync.PhoneNumberSyncEntity;
import net.dankito.sync.PhoneNumberType;
import net.dankito.sync.SyncConfiguration;
import net.dankito.sync.SyncEntity;
import net.dankito.sync.SyncEntityLocalLookupKeys;
import net.dankito.sync.SyncJobItem;
import net.dankito.sync.SyncModuleConfiguration;
import net.dankito.sync.SyncState;
import net.dankito.sync.data.IDataManager;
import net.dankito.sync.devices.DiscoveredDevice;
import net.dankito.sync.devices.IDevicesManager;
import net.dankito.sync.localization.Localization;
import net.dankito.sync.persistence.CouchbaseLiteEntityManagerBase;
import net.dankito.sync.persistence.CouchbaseLiteEntityManagerJava;
import net.dankito.sync.persistence.EntityManagerConfiguration;
import net.dankito.sync.persistence.IEntityManager;
import net.dankito.sync.synchronization.files.FileSender;
import net.dankito.sync.synchronization.files.FileSyncJobItem;
import net.dankito.sync.synchronization.files.FileSyncService;
import net.dankito.sync.synchronization.merge.IDataMerger;
import net.dankito.sync.synchronization.merge.JpaMetadataBasedDataMerger;
import net.dankito.sync.synchronization.modules.FileSyncModule;
import net.dankito.sync.synchronization.modules.ISyncModule;
import net.dankito.sync.synchronization.util.SyncConfigurationManagerStub;
import net.dankito.sync.synchronization.util.SyncModuleMock;
import net.dankito.utils.ObjectHolder;
import net.dankito.utils.ThreadPool;
import net.dankito.utils.services.IFileStorageService;
import net.dankito.utils.services.JavaFileStorageService;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

public class SyncConfigurationManagerBaseTest {

  protected static final String TEST_CONTACT_SYNC_ENTITY_01_LOCAL_ID = "01";
  protected static final String TEST_CONTACT_SYNC_ENTITY_01_DISPLAY_NAME = "Mandela";
  protected static final String TEST_UPDATED_CONTACT_SYNC_ENTITY_01_DISPLAY_NAME = "Nelson Mandela";

  protected static final String TEST_CONTACT_SYNC_ENTITY_01_PHONE_NUMBER_01_LOOKUP_KEY = "phone_1.1";
  protected static final String TEST_CONTACT_SYNC_ENTITY_01_PHONE_NUMBER_01 = "0123456789";
  protected static final String TEST_UPDATED_CONTACT_SYNC_ENTITY_01_PHONE_NUMBER_01 = "01234567890";
  protected static final PhoneNumberType TEST_CONTACT_SYNC_ENTITY_01_PHONE_NUMBER_TYPE_01 = PhoneNumberType.MOBILE;
  protected static final PhoneNumberType TEST_UPDATED_CONTACT_SYNC_ENTITY_01_PHONE_NUMBER_TYPE_01 = PhoneNumberType.WORK;
  protected static final String TEST_CONTACT_SYNC_ENTITY_01_PHONE_NUMBER_02_LOOKUP_KEY = "phone_1.2";
  protected static final String TEST_CONTACT_SYNC_ENTITY_01_PHONE_NUMBER_02 = "03456789129";
  protected static final PhoneNumberType TEST_CONTACT_SYNC_ENTITY_01_PHONE_NUMBER_TYPE_02 = PhoneNumberType.HOME;
  protected static final String TEST_CONTACT_SYNC_ENTITY_01_PHONE_NUMBER_03_LOOKUP_KEY = "phone_1.3";
  protected static final String TEST_CONTACT_SYNC_ENTITY_01_PHONE_NUMBER_03 = "0789123456";
  protected static final PhoneNumberType TEST_CONTACT_SYNC_ENTITY_01_PHONE_NUMBER_TYPE_03 = PhoneNumberType.OTHER;

  protected static final String TEST_CONTACT_SYNC_ENTITY_01_EMAIL_01_LOOKUP_KEY = "email_1.1";
  protected static final String TEST_CONTACT_SYNC_ENTITY_01_EMAIL_ADDRESS_01 = "nelson@heroes.net";
  protected static final String TEST_UPDATED_CONTACT_SYNC_ENTITY_01_EMAIL_ADDRESS_01 = "nelson@greatest-heroes.net";
  protected static final EmailType TEST_CONTACT_SYNC_ENTITY_01_EMAIL_TYPE_01 = EmailType.WORK;
  protected static final EmailType TEST_UPDATED_CONTACT_SYNC_ENTITY_01_EMAIL_TYPE_01 = EmailType.HOME;

  protected static final String TEST_CONTACT_SYNC_ENTITY_02_LOCAL_ID = "02";
  protected static final String TEST_CONTACT_SYNC_ENTITY_02_DISPLAY_NAME = "Gandhi";

  protected static final String TEST_CONTACT_SYNC_ENTITY_02_PHONE_NUMBER_01_LOOKUP_KEY = "phone_2.1";
  protected static final String TEST_CONTACT_SYNC_ENTITY_02_PHONE_NUMBER_01 = "0987654321";
  protected static final PhoneNumberType TEST_CONTACT_SYNC_ENTITY_02_PHONE_NUMBER_TYPE_01 = PhoneNumberType.WORK;

  protected static final String TEST_CONTACT_SYNC_ENTITY_02_EMAIL_01_LOOKUP_KEY = "email_2.1";
  protected static final String TEST_CONTACT_SYNC_ENTITY_02_EMAIL_ADDRESS_01 = "mahatma@heroes.net";
  protected static final EmailType TEST_CONTACT_SYNC_ENTITY_02_EMAIL_TYPE_01 = EmailType.WORK;
  protected static final String TEST_CONTACT_SYNC_ENTITY_02_EMAIL_02_LOOKUP_KEY = "email_2.2";
  protected static final String TEST_CONTACT_SYNC_ENTITY_02_EMAIL_ADDRESS_02 = "private@gandhi.net";
  protected static final EmailType TEST_CONTACT_SYNC_ENTITY_02_EMAIL_TYPE_02 = EmailType.HOME;
  protected static final String TEST_CONTACT_SYNC_ENTITY_02_EMAIL_03_LOOKUP_KEY = "email_2.3";
  protected static final String TEST_CONTACT_SYNC_ENTITY_02_EMAIL_ADDRESS_03 = "free-india@british-empire.co.uk";
  protected static final EmailType TEST_CONTACT_SYNC_ENTITY_02_EMAIL_TYPE_03 = EmailType.OTHER;


  protected static final String FILE_SYNC_MODULE_TYPE = "File";

  protected static final String TEST_FILE_SYNC_ENTITY_01_LOCAL_ID = "01";
  protected static final String TEST_FILE_SYNC_ENTITY_01_PATH = "/tmp/love";
  protected static final String TEST_UPDATED_FILE_SYNC_ENTITY_01_PATH = "/tmp/eternal-love";


  protected SyncConfigurationManagerBase underTest;


  protected ISyncManager syncManager;

  protected IEntityManager entityManager;

  protected IDevicesManager devicesManager;

  protected IDataManager dataManager;

  protected FileSender fileSender;

  protected LocalConfig localConfig;

  protected Device remoteDevice;

  protected DiscoveredDevice discoveredRemoteDevice;

  protected SyncConfiguration syncConfiguration;

  protected SyncModuleMock syncModuleMock;

  protected FileSyncModule fileSyncModule;

  protected List<SyncEntityChangeListener> fileSyncModulesEntityChangeListeners = new ArrayList<>();

  protected SyncModuleConfiguration syncModuleConfiguration;

  protected SyncModuleConfiguration fileSyncModuleConfiguration;

  protected List<SyncEntity> entitiesToReturnFromReadAllEntitiesAsync = new ArrayList<>();

  protected SynchronizationListener registeredSynchronizationListener;


  @Before
  public void setUp() throws Exception {
    syncManager = mock(ISyncManager.class);
    doAnswer(new Answer() {
      @Override
      public Object answer(InvocationOnMock invocation) throws Throwable {
        registeredSynchronizationListener = (SynchronizationListener)invocation.getArguments()[0];
        return null;
      }
    }).when(syncManager).addSynchronizationListener(any(SynchronizationListener.class));

    EntityManagerConfiguration entityManagerConfiguration = new EntityManagerConfiguration("testData", 1);
    deleteDatabase(entityManagerConfiguration.getDataFolder());
    entityManager = new CouchbaseLiteEntityManagerJava(entityManagerConfiguration);

    IDataMerger dataMerger = new JpaMetadataBasedDataMerger((CouchbaseLiteEntityManagerBase)entityManager);

    Device localDevice = new Device("local");
    localDevice.setName("local");
    localConfig = new LocalConfig(localDevice);

    dataManager = mock(IDataManager.class);
    when(dataManager.getLocalConfig()).thenReturn(localConfig);

    fileSender = mock(FileSender.class);

    remoteDevice = new Device("remote");
    remoteDevice.setName("remote");
    discoveredRemoteDevice = new DiscoveredDevice(remoteDevice, "1.1.1.1");

    devicesManager = mock(IDevicesManager.class);
    when(devicesManager.getDiscoveredDeviceForDevice(remoteDevice)).thenReturn(discoveredRemoteDevice);

    syncModuleMock = new SyncModuleMock(entitiesToReturnFromReadAllEntitiesAsync);
    syncModuleConfiguration = new SyncModuleConfiguration(syncModuleMock.getSyncEntityTypeItCanHandle());
    entityManager.persistEntity(syncModuleConfiguration);

    fileSyncModule = new FileSyncModule(mock(Localization.class), new FileSyncService(entityManager), mock(IFileStorageService.class)) {
      @Override
      public String getSyncEntityTypeItCanHandle() {
        return FILE_SYNC_MODULE_TYPE;
      }

      @Override
      public void addSyncEntityChangeListener(SyncEntityChangeListener listener) {
        fileSyncModulesEntityChangeListeners.add(listener);
      }

      @Override
      public void removeSyncEntityChangeListener(SyncEntityChangeListener listener) {
        fileSyncModulesEntityChangeListeners.remove(listener);
      }
    };
    fileSyncModuleConfiguration = new SyncModuleConfiguration(fileSyncModule.getSyncEntityTypeItCanHandle());
    fileSyncModuleConfiguration.setEnabled(true);
    entityManager.persistEntity(fileSyncModuleConfiguration);

    syncConfiguration = new SyncConfiguration(localConfig.getLocalDevice(), remoteDevice,
        Arrays.asList(new SyncModuleConfiguration[] {  syncModuleConfiguration, fileSyncModuleConfiguration }));
    entityManager.persistEntity(syncConfiguration);

    localDevice.addSourceSyncConfiguration(syncConfiguration);
    remoteDevice.addDestinationSyncConfiguration(syncConfiguration);
    entityManager.persistEntity(localDevice);
    entityManager.persistEntity(remoteDevice);

    underTest = new SyncConfigurationManagerStub(syncManager, dataManager, entityManager, devicesManager, dataMerger, fileSender, new JavaFileStorageService(), new ThreadPool(), discoveredRemoteDevice);


    SyncConfigurationManagerStub syncConfigurationManagerStub = (SyncConfigurationManagerStub)underTest;
    List<ISyncModule> mockedAvailableSyncModules = new ArrayList<>();
    mockedAvailableSyncModules.add(syncModuleMock);
    mockedAvailableSyncModules.add(fileSyncModule);
    syncConfigurationManagerStub.setMockedAvailableSyncModules(mockedAvailableSyncModules);

    syncConfigurationManagerStub.startContinuousSynchronizationWithDevice(discoveredRemoteDevice, syncConfiguration);
  }

  @After
  public void tearDown() {
    entityManager.close();

    deleteDatabase(entityManager.getDatabasePath());
  }

  protected void deleteDatabase(String databasePath) {
    new JavaFileStorageService().deleteFolderRecursively(databasePath);
  }


  @Test
  public void syncNewEntities() {
    List<SyncEntity> testEntities = new ArrayList<>();

    ContactSyncEntity testEntity01 = new ContactSyncEntity();
    testEntity01.setLocalLookupKey(TEST_CONTACT_SYNC_ENTITY_01_LOCAL_ID);
    testEntity01.setDisplayName(TEST_CONTACT_SYNC_ENTITY_01_DISPLAY_NAME);
    testEntities.add(testEntity01);

    ContactSyncEntity testEntity02 = new ContactSyncEntity();
    testEntity02.setLocalLookupKey(TEST_CONTACT_SYNC_ENTITY_02_LOCAL_ID);
    testEntity02.setDisplayName(TEST_CONTACT_SYNC_ENTITY_02_DISPLAY_NAME);
    testEntities.add(testEntity02);

    Assert.assertEquals(0, entityManager.getAllEntitiesOfType(ContactSyncEntity.class).size());
    Assert.assertEquals(0, entityManager.getAllEntitiesOfType(SyncJobItem.class).size());
    Assert.assertEquals(0, entityManager.getAllEntitiesOfType(SyncEntityLocalLookupKeys.class).size());


    mockSynchronizeEntitiesWithDevice(testEntities);


    Assert.assertEquals(2, entityManager.getAllEntitiesOfType(ContactSyncEntity.class).size());
    Assert.assertEquals(2, entityManager.getAllEntitiesOfType(SyncJobItem.class).size());
    Assert.assertEquals(2, entityManager.getAllEntitiesOfType(SyncEntityLocalLookupKeys.class).size());
  }

  @Test
  public void syncNewEntitiesWithPhoneNumberSyncEntityProperties() {
    List<SyncEntity> testEntities = new ArrayList<>();

    ContactSyncEntity testEntity01 = new ContactSyncEntity();
    testEntity01.setLocalLookupKey(TEST_CONTACT_SYNC_ENTITY_01_LOCAL_ID);
    testEntity01.setDisplayName(TEST_CONTACT_SYNC_ENTITY_01_DISPLAY_NAME);
    testEntity01.addPhoneNumber(createTestPhoneNumber(TEST_CONTACT_SYNC_ENTITY_01_PHONE_NUMBER_01_LOOKUP_KEY, TEST_CONTACT_SYNC_ENTITY_01_PHONE_NUMBER_01, TEST_CONTACT_SYNC_ENTITY_01_PHONE_NUMBER_TYPE_01));
    testEntity01.addPhoneNumber(createTestPhoneNumber(TEST_CONTACT_SYNC_ENTITY_01_PHONE_NUMBER_02_LOOKUP_KEY, TEST_CONTACT_SYNC_ENTITY_01_PHONE_NUMBER_02, TEST_CONTACT_SYNC_ENTITY_01_PHONE_NUMBER_TYPE_02));
    testEntity01.addPhoneNumber(createTestPhoneNumber(TEST_CONTACT_SYNC_ENTITY_01_PHONE_NUMBER_03_LOOKUP_KEY, TEST_CONTACT_SYNC_ENTITY_01_PHONE_NUMBER_03, TEST_CONTACT_SYNC_ENTITY_01_PHONE_NUMBER_TYPE_03));
    testEntities.add(testEntity01);

    ContactSyncEntity testEntity02 = new ContactSyncEntity();
    testEntity02.setLocalLookupKey(TEST_CONTACT_SYNC_ENTITY_02_LOCAL_ID);
    testEntity02.setDisplayName(TEST_CONTACT_SYNC_ENTITY_02_DISPLAY_NAME);
    testEntity02.addPhoneNumber(createTestPhoneNumber(TEST_CONTACT_SYNC_ENTITY_02_PHONE_NUMBER_01_LOOKUP_KEY, TEST_CONTACT_SYNC_ENTITY_02_PHONE_NUMBER_01, TEST_CONTACT_SYNC_ENTITY_02_PHONE_NUMBER_TYPE_01));
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
  public void syncNewEntitiesWithEmailSyncEntityProperties() {
    List<SyncEntity> testEntities = new ArrayList<>();

    ContactSyncEntity testEntity01 = new ContactSyncEntity();
    testEntity01.setLocalLookupKey(TEST_CONTACT_SYNC_ENTITY_01_LOCAL_ID);
    testEntity01.setDisplayName(TEST_CONTACT_SYNC_ENTITY_01_DISPLAY_NAME);
    testEntity01.addEmailAddress(createTestEmail(TEST_CONTACT_SYNC_ENTITY_01_EMAIL_01_LOOKUP_KEY, TEST_CONTACT_SYNC_ENTITY_01_EMAIL_ADDRESS_01, TEST_CONTACT_SYNC_ENTITY_01_EMAIL_TYPE_01));
    testEntities.add(testEntity01);

    ContactSyncEntity testEntity02 = new ContactSyncEntity();
    testEntity02.setLocalLookupKey(TEST_CONTACT_SYNC_ENTITY_02_LOCAL_ID);
    testEntity02.setDisplayName(TEST_CONTACT_SYNC_ENTITY_02_DISPLAY_NAME);
    testEntity02.addEmailAddress(createTestEmail(TEST_CONTACT_SYNC_ENTITY_02_EMAIL_01_LOOKUP_KEY, TEST_CONTACT_SYNC_ENTITY_02_EMAIL_ADDRESS_01, TEST_CONTACT_SYNC_ENTITY_02_EMAIL_TYPE_01));
    testEntity02.addEmailAddress(createTestEmail(TEST_CONTACT_SYNC_ENTITY_02_EMAIL_02_LOOKUP_KEY, TEST_CONTACT_SYNC_ENTITY_02_EMAIL_ADDRESS_02, TEST_CONTACT_SYNC_ENTITY_02_EMAIL_TYPE_02));
    testEntity02.addEmailAddress(createTestEmail(TEST_CONTACT_SYNC_ENTITY_02_EMAIL_03_LOOKUP_KEY, TEST_CONTACT_SYNC_ENTITY_02_EMAIL_ADDRESS_03, TEST_CONTACT_SYNC_ENTITY_02_EMAIL_TYPE_03));
    testEntities.add(testEntity02);

    Assert.assertEquals(0, entityManager.getAllEntitiesOfType(ContactSyncEntity.class).size());
    Assert.assertEquals(0, entityManager.getAllEntitiesOfType(EmailSyncEntity.class).size());
    Assert.assertEquals(0, entityManager.getAllEntitiesOfType(SyncJobItem.class).size());
    Assert.assertEquals(0, entityManager.getAllEntitiesOfType(SyncEntityLocalLookupKeys.class).size());


    mockSynchronizeEntitiesWithDevice(testEntities);


    Assert.assertEquals(2, entityManager.getAllEntitiesOfType(ContactSyncEntity.class).size());
    Assert.assertEquals(4, entityManager.getAllEntitiesOfType(EmailSyncEntity.class).size());
    Assert.assertEquals(2, entityManager.getAllEntitiesOfType(SyncJobItem.class).size());
    Assert.assertEquals(6, entityManager.getAllEntitiesOfType(SyncEntityLocalLookupKeys.class).size());

    List<ContactSyncEntity> contacts = entityManager.getAllEntitiesOfType(ContactSyncEntity.class);
    for(ContactSyncEntity contact : contacts) {
      if(TEST_CONTACT_SYNC_ENTITY_01_DISPLAY_NAME.equals(contact.getDisplayName())) {
        Assert.assertEquals(1, contact.getEmailAddresses().size());
      }
      else if(TEST_CONTACT_SYNC_ENTITY_02_DISPLAY_NAME.equals(contact.getDisplayName())) {
        Assert.assertEquals(3, contact.getEmailAddresses().size());
      }
    }
  }


  @Test
  public void syncPersistedButUnsyncedEntities() {
    List<SyncEntity> testEntities = new ArrayList<>();
    SyncEntity testEntity01 = new ContactSyncEntity();
    testEntity01.setLocalLookupKey(TEST_CONTACT_SYNC_ENTITY_01_LOCAL_ID);
    SyncEntity testEntity02 = new ContactSyncEntity();
    testEntity02.setLocalLookupKey(TEST_CONTACT_SYNC_ENTITY_02_LOCAL_ID);
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
    Assert.assertEquals(2, entityManager.getAllEntitiesOfType(SyncJobItem.class).size());
    Assert.assertEquals(2, entityManager.getAllEntitiesOfType(SyncEntityLocalLookupKeys.class).size());


    testEntities.clear();

    ContactSyncEntity updatedTestEntity01 = new ContactSyncEntity();
    updatedTestEntity01.setLocalLookupKey(TEST_CONTACT_SYNC_ENTITY_01_LOCAL_ID);
    updatedTestEntity01.setDisplayName(TEST_CONTACT_SYNC_ENTITY_01_DISPLAY_NAME);
    updatedTestEntity01.setLastModifiedOnDevice(new Date());
    testEntities.add(updatedTestEntity01);

    ContactSyncEntity updatedTestEntity02 = new ContactSyncEntity();
    updatedTestEntity02.setLocalLookupKey(TEST_CONTACT_SYNC_ENTITY_02_LOCAL_ID);
    updatedTestEntity02.setDisplayName(TEST_CONTACT_SYNC_ENTITY_02_DISPLAY_NAME);
    updatedTestEntity02.setLastModifiedOnDevice(new Date());
    testEntities.add(updatedTestEntity02);


    mockSynchronizeEntitiesWithDevice(testEntities);


    Assert.assertEquals(2, entityManager.getAllEntitiesOfType(ContactSyncEntity.class).size());
    Assert.assertEquals(4, entityManager.getAllEntitiesOfType(SyncJobItem.class).size());
    Assert.assertEquals(2, entityManager.getAllEntitiesOfType(SyncEntityLocalLookupKeys.class).size());

    List<ContactSyncEntity> contacts = entityManager.getAllEntitiesOfType(ContactSyncEntity.class);
    boolean entity01Updated = false, entity02Updated = false;

    for(ContactSyncEntity retrievedContact : contacts) {
      if(TEST_CONTACT_SYNC_ENTITY_01_DISPLAY_NAME.equals(retrievedContact.getDisplayName())) {
        entity01Updated = true;
      }
      else {
        entity02Updated = true;
      }
    }

    assertThat(entity01Updated, is(true));
    assertThat(entity02Updated, is(true));
  }

  @Test
  public void syncUpdatedEntitiesWithPhoneNumbersSyncEntityProperties() {
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
    updatedTestEntity01.addPhoneNumber(createTestPhoneNumber(TEST_CONTACT_SYNC_ENTITY_01_PHONE_NUMBER_02_LOOKUP_KEY, TEST_CONTACT_SYNC_ENTITY_01_PHONE_NUMBER_02, TEST_CONTACT_SYNC_ENTITY_01_PHONE_NUMBER_TYPE_02));
    updatedTestEntity01.addPhoneNumber(createTestPhoneNumber(TEST_CONTACT_SYNC_ENTITY_01_PHONE_NUMBER_03_LOOKUP_KEY, TEST_CONTACT_SYNC_ENTITY_01_PHONE_NUMBER_03, TEST_CONTACT_SYNC_ENTITY_01_PHONE_NUMBER_TYPE_03));
    updatedTestEntity01.setLastModifiedOnDevice(new Date());
    testEntities.add(updatedTestEntity01);

    ContactSyncEntity updatedTestEntity02 = new ContactSyncEntity();
    updatedTestEntity02.setLocalLookupKey(TEST_CONTACT_SYNC_ENTITY_02_LOCAL_ID);
    updatedTestEntity02.setDisplayName(TEST_CONTACT_SYNC_ENTITY_02_DISPLAY_NAME);
    updatedTestEntity02.addPhoneNumber(createTestPhoneNumber(TEST_CONTACT_SYNC_ENTITY_02_PHONE_NUMBER_01_LOOKUP_KEY, TEST_CONTACT_SYNC_ENTITY_02_PHONE_NUMBER_01, TEST_CONTACT_SYNC_ENTITY_02_PHONE_NUMBER_TYPE_01));
    updatedTestEntity02.setLastModifiedOnDevice(new Date());
    testEntities.add(updatedTestEntity02);


    mockSynchronizeEntitiesWithDevice(testEntities);


    Assert.assertEquals(2, entityManager.getAllEntitiesOfType(ContactSyncEntity.class).size());
    Assert.assertEquals(3, entityManager.getAllEntitiesOfType(PhoneNumberSyncEntity.class).size());
    Assert.assertEquals(4, entityManager.getAllEntitiesOfType(SyncJobItem.class).size());
    Assert.assertEquals(5, entityManager.getAllEntitiesOfType(SyncEntityLocalLookupKeys.class).size());

    List<ContactSyncEntity> contacts = entityManager.getAllEntitiesOfType(ContactSyncEntity.class);
    ObjectHolder<Boolean> firstPhoneNumberRetrieved = new ObjectHolder<>(false), secondPhoneNumberRetrieved = new ObjectHolder<>(false);

    for(ContactSyncEntity retrievedContact : contacts) {
      Assert.assertNotEquals(0, retrievedContact.getPhoneNumbers().size());
      PhoneNumberSyncEntity retrievedPhoneNumber = retrievedContact.getPhoneNumbers().get(0);

      if(TEST_CONTACT_SYNC_ENTITY_01_DISPLAY_NAME.equals(retrievedContact.getDisplayName())) {
        testUpdatedPhoneNumberOfFirstContact(retrievedPhoneNumber, firstPhoneNumberRetrieved, secondPhoneNumberRetrieved);

        PhoneNumberSyncEntity secondRetrievedPhoneNumber = retrievedContact.getPhoneNumbers().get(1);
        testUpdatedPhoneNumberOfFirstContact(secondRetrievedPhoneNumber, firstPhoneNumberRetrieved, secondPhoneNumberRetrieved);
      }
      else {
        Assert.assertEquals(TEST_CONTACT_SYNC_ENTITY_02_PHONE_NUMBER_01, retrievedPhoneNumber.getNumber());
        Assert.assertEquals(TEST_CONTACT_SYNC_ENTITY_02_PHONE_NUMBER_TYPE_01, retrievedPhoneNumber.getType());
      }
    }

    assertThat(firstPhoneNumberRetrieved.getObject(), is(true));
    assertThat(secondPhoneNumberRetrieved.getObject(), is(true));
  }

  protected void testUpdatedPhoneNumberOfFirstContact(PhoneNumberSyncEntity retrievedPhoneNumber, ObjectHolder<Boolean> firstPhoneNumberRetrieved, ObjectHolder<Boolean> secondPhoneNumberRetrieved) {
    if(TEST_CONTACT_SYNC_ENTITY_01_PHONE_NUMBER_02.equals(retrievedPhoneNumber.getNumber())) {
      Assert.assertEquals(TEST_CONTACT_SYNC_ENTITY_01_PHONE_NUMBER_TYPE_02, retrievedPhoneNumber.getType());
      firstPhoneNumberRetrieved.setObject(true);
    }
    else {
      Assert.assertEquals(TEST_CONTACT_SYNC_ENTITY_01_PHONE_NUMBER_03, retrievedPhoneNumber.getNumber());
      Assert.assertEquals(TEST_CONTACT_SYNC_ENTITY_01_PHONE_NUMBER_TYPE_03, retrievedPhoneNumber.getType());
      secondPhoneNumberRetrieved.setObject(true);
    }
  }

  @Test
  public void syncUpdatedEntitiesWithEmailsSyncEntityProperties() {
    List<SyncEntity> testEntities = new ArrayList<>();
    ContactSyncEntity testEntity01 = new ContactSyncEntity();
    testEntity01.setLocalLookupKey(TEST_CONTACT_SYNC_ENTITY_01_LOCAL_ID);
    testEntities.add(testEntity01);
    ContactSyncEntity testEntity02 = new ContactSyncEntity();
    testEntity02.setLocalLookupKey(TEST_CONTACT_SYNC_ENTITY_02_LOCAL_ID);
    testEntities.add(testEntity02);

    mockSynchronizeEntitiesWithDevice(testEntities);

    Assert.assertEquals(2, entityManager.getAllEntitiesOfType(ContactSyncEntity.class).size());
    Assert.assertEquals(0, entityManager.getAllEntitiesOfType(EmailSyncEntity.class).size());
    Assert.assertEquals(2, entityManager.getAllEntitiesOfType(SyncJobItem.class).size());
    Assert.assertEquals(2, entityManager.getAllEntitiesOfType(SyncEntityLocalLookupKeys.class).size());


    testEntities.clear();

    ContactSyncEntity updatedTestEntity01 = new ContactSyncEntity();
    updatedTestEntity01.setLocalLookupKey(TEST_CONTACT_SYNC_ENTITY_01_LOCAL_ID);
    updatedTestEntity01.setDisplayName(TEST_CONTACT_SYNC_ENTITY_01_DISPLAY_NAME);
    updatedTestEntity01.addEmailAddress(createTestEmail(TEST_CONTACT_SYNC_ENTITY_01_EMAIL_01_LOOKUP_KEY, TEST_CONTACT_SYNC_ENTITY_01_EMAIL_ADDRESS_01, TEST_CONTACT_SYNC_ENTITY_01_EMAIL_TYPE_01));
    updatedTestEntity01.setLastModifiedOnDevice(new Date());
    testEntities.add(updatedTestEntity01);

    ContactSyncEntity updatedTestEntity02 = new ContactSyncEntity();
    updatedTestEntity02.setLocalLookupKey(TEST_CONTACT_SYNC_ENTITY_02_LOCAL_ID);
    updatedTestEntity02.setDisplayName(TEST_CONTACT_SYNC_ENTITY_02_DISPLAY_NAME);
    updatedTestEntity02.addEmailAddress(createTestEmail(TEST_CONTACT_SYNC_ENTITY_02_EMAIL_02_LOOKUP_KEY, TEST_CONTACT_SYNC_ENTITY_02_EMAIL_ADDRESS_02, TEST_CONTACT_SYNC_ENTITY_02_EMAIL_TYPE_02));
    updatedTestEntity02.addEmailAddress(createTestEmail(TEST_CONTACT_SYNC_ENTITY_02_EMAIL_03_LOOKUP_KEY, TEST_CONTACT_SYNC_ENTITY_02_EMAIL_ADDRESS_03, TEST_CONTACT_SYNC_ENTITY_02_EMAIL_TYPE_03));
    updatedTestEntity02.setLastModifiedOnDevice(new Date());
    testEntities.add(updatedTestEntity02);


    mockSynchronizeEntitiesWithDevice(testEntities);


    Assert.assertEquals(2, entityManager.getAllEntitiesOfType(ContactSyncEntity.class).size());
    Assert.assertEquals(3, entityManager.getAllEntitiesOfType(EmailSyncEntity.class).size());
    Assert.assertEquals(4, entityManager.getAllEntitiesOfType(SyncJobItem.class).size());
    Assert.assertEquals(5, entityManager.getAllEntitiesOfType(SyncEntityLocalLookupKeys.class).size());

    List<ContactSyncEntity> contacts = entityManager.getAllEntitiesOfType(ContactSyncEntity.class);
    ObjectHolder<Boolean> firstEmailRetrieved = new ObjectHolder<>(false), secondEmailRetrieved = new ObjectHolder<>(false);

    for(ContactSyncEntity retrievedContact : contacts) {
      Assert.assertNotEquals(0, retrievedContact.getEmailAddresses().size());
      EmailSyncEntity retrievedEmail = retrievedContact.getEmailAddresses().get(0);

      if(TEST_CONTACT_SYNC_ENTITY_01_DISPLAY_NAME.equals(retrievedContact.getDisplayName())) {
        Assert.assertEquals(TEST_CONTACT_SYNC_ENTITY_01_EMAIL_ADDRESS_01, retrievedEmail.getAddress());
        Assert.assertEquals(TEST_CONTACT_SYNC_ENTITY_01_EMAIL_TYPE_01, retrievedEmail.getType());
      }
      else {
        testUpdatedEmailOfSecondContact(retrievedEmail, firstEmailRetrieved, secondEmailRetrieved);

        EmailSyncEntity secondRetrievedEmail = retrievedContact.getEmailAddresses().get(1);
        testUpdatedEmailOfSecondContact(secondRetrievedEmail, firstEmailRetrieved, secondEmailRetrieved);
      }
    }

    assertThat(firstEmailRetrieved.getObject(), is(true));
    assertThat(secondEmailRetrieved.getObject(), is(true));
  }

  protected void testUpdatedEmailOfSecondContact(EmailSyncEntity retrievedEmail, ObjectHolder<Boolean> firstEmailRetrieved, ObjectHolder<Boolean> secondEmailRetrieved) {
    if(TEST_CONTACT_SYNC_ENTITY_02_EMAIL_ADDRESS_02.equals(retrievedEmail.getAddress())) {
      Assert.assertEquals(TEST_CONTACT_SYNC_ENTITY_02_EMAIL_TYPE_02, retrievedEmail.getType());
      firstEmailRetrieved.setObject(true);
    }
    else {
      Assert.assertEquals(TEST_CONTACT_SYNC_ENTITY_02_EMAIL_ADDRESS_03, retrievedEmail.getAddress());
      Assert.assertEquals(TEST_CONTACT_SYNC_ENTITY_02_EMAIL_TYPE_03, retrievedEmail.getType());
      secondEmailRetrieved.setObject(true);
    }
  }

  // only a SyncEntity Property has been updated - check if this change is detected and synchronized
  @Test
  public void syncUpdatedProperty() {
    List<SyncEntity> testEntities = new ArrayList<>();
    ContactSyncEntity testEntity01 = new ContactSyncEntity();
    testEntity01.setLocalLookupKey(TEST_CONTACT_SYNC_ENTITY_01_LOCAL_ID);
    testEntity01.setDisplayName(TEST_CONTACT_SYNC_ENTITY_01_DISPLAY_NAME);
    testEntity01.addPhoneNumber(createTestPhoneNumber(TEST_CONTACT_SYNC_ENTITY_01_PHONE_NUMBER_01_LOOKUP_KEY, TEST_CONTACT_SYNC_ENTITY_01_PHONE_NUMBER_01, TEST_CONTACT_SYNC_ENTITY_01_PHONE_NUMBER_TYPE_01));
    testEntity01.addEmailAddress(createTestEmail(TEST_CONTACT_SYNC_ENTITY_01_EMAIL_01_LOOKUP_KEY, TEST_CONTACT_SYNC_ENTITY_01_EMAIL_ADDRESS_01, TEST_CONTACT_SYNC_ENTITY_01_EMAIL_TYPE_01));
    testEntities.add(testEntity01);

    mockSynchronizeEntitiesWithDevice(testEntities);

    Assert.assertEquals(1, entityManager.getAllEntitiesOfType(ContactSyncEntity.class).size());
    Assert.assertEquals(1, entityManager.getAllEntitiesOfType(PhoneNumberSyncEntity.class).size());
    Assert.assertEquals(1, entityManager.getAllEntitiesOfType(EmailSyncEntity.class).size());
    Assert.assertEquals(1, entityManager.getAllEntitiesOfType(SyncJobItem.class).size());
    Assert.assertEquals(3, entityManager.getAllEntitiesOfType(SyncEntityLocalLookupKeys.class).size());


    testEntities.clear();

    ContactSyncEntity updatedTestEntity01 = new ContactSyncEntity();
    updatedTestEntity01.setLocalLookupKey(TEST_CONTACT_SYNC_ENTITY_01_LOCAL_ID);
    updatedTestEntity01.setDisplayName(TEST_CONTACT_SYNC_ENTITY_01_DISPLAY_NAME);
    updatedTestEntity01.addPhoneNumber(createTestPhoneNumber(TEST_CONTACT_SYNC_ENTITY_01_PHONE_NUMBER_01_LOOKUP_KEY, TEST_UPDATED_CONTACT_SYNC_ENTITY_01_PHONE_NUMBER_01, TEST_UPDATED_CONTACT_SYNC_ENTITY_01_PHONE_NUMBER_TYPE_01));
    updatedTestEntity01.addEmailAddress(createTestEmail(TEST_CONTACT_SYNC_ENTITY_01_EMAIL_01_LOOKUP_KEY, TEST_UPDATED_CONTACT_SYNC_ENTITY_01_EMAIL_ADDRESS_01, TEST_UPDATED_CONTACT_SYNC_ENTITY_01_EMAIL_TYPE_01));
    testEntities.add(updatedTestEntity01);


    mockSynchronizeEntitiesWithDevice(testEntities);


    Assert.assertEquals(1, entityManager.getAllEntitiesOfType(ContactSyncEntity.class).size());
    Assert.assertEquals(1, entityManager.getAllEntitiesOfType(PhoneNumberSyncEntity.class).size());
    Assert.assertEquals(1, entityManager.getAllEntitiesOfType(EmailSyncEntity.class).size());
    Assert.assertEquals(2, entityManager.getAllEntitiesOfType(SyncJobItem.class).size());
    Assert.assertEquals(3, entityManager.getAllEntitiesOfType(SyncEntityLocalLookupKeys.class).size());

    ContactSyncEntity updatedContact = entityManager.getAllEntitiesOfType(ContactSyncEntity.class).get(0);

    assertThat(updatedContact.getPhoneNumbers().get(0).getNumber(), is(TEST_UPDATED_CONTACT_SYNC_ENTITY_01_PHONE_NUMBER_01));
    assertThat(updatedContact.getPhoneNumbers().get(0).getType(), is(TEST_UPDATED_CONTACT_SYNC_ENTITY_01_PHONE_NUMBER_TYPE_01));

    assertThat(updatedContact.getEmailAddresses().get(0).getAddress(), is(TEST_UPDATED_CONTACT_SYNC_ENTITY_01_EMAIL_ADDRESS_01));
    assertThat(updatedContact.getEmailAddresses().get(0).getType(), is(TEST_UPDATED_CONTACT_SYNC_ENTITY_01_EMAIL_TYPE_01));
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

  @Test
  public void deleteProperty() {
    List<SyncEntity> testEntities = new ArrayList<>();
    ContactSyncEntity testEntity01 = new ContactSyncEntity();
    testEntity01.setLocalLookupKey(TEST_CONTACT_SYNC_ENTITY_01_LOCAL_ID);
    testEntity01.setDisplayName(TEST_CONTACT_SYNC_ENTITY_01_DISPLAY_NAME);
    PhoneNumberSyncEntity phoneNumberToDelete = createTestPhoneNumber(TEST_CONTACT_SYNC_ENTITY_01_PHONE_NUMBER_01_LOOKUP_KEY, TEST_CONTACT_SYNC_ENTITY_01_PHONE_NUMBER_01, TEST_CONTACT_SYNC_ENTITY_01_PHONE_NUMBER_TYPE_01);
    EmailSyncEntity emailToDelete = createTestEmail(TEST_CONTACT_SYNC_ENTITY_01_EMAIL_01_LOOKUP_KEY, TEST_CONTACT_SYNC_ENTITY_01_EMAIL_ADDRESS_01, TEST_CONTACT_SYNC_ENTITY_01_EMAIL_TYPE_01);
    testEntity01.addPhoneNumber(phoneNumberToDelete);
    testEntity01.addEmailAddress(emailToDelete);
    testEntities.add(testEntity01);

    mockSynchronizeEntitiesWithDevice(testEntities);

    Assert.assertEquals(1, entityManager.getAllEntitiesOfType(ContactSyncEntity.class).size());
    Assert.assertEquals(1, entityManager.getAllEntitiesOfType(PhoneNumberSyncEntity.class).size());
    Assert.assertEquals(1, entityManager.getAllEntitiesOfType(EmailSyncEntity.class).size());
    Assert.assertEquals(1, entityManager.getAllEntitiesOfType(SyncJobItem.class).size());
    Assert.assertEquals(3, entityManager.getAllEntitiesOfType(SyncEntityLocalLookupKeys.class).size());


    testEntities.clear();

    ContactSyncEntity updatedTestEntity01 = new ContactSyncEntity();
    updatedTestEntity01.setLocalLookupKey(TEST_CONTACT_SYNC_ENTITY_01_LOCAL_ID);
    updatedTestEntity01.setDisplayName(TEST_CONTACT_SYNC_ENTITY_01_DISPLAY_NAME);
    updatedTestEntity01.addPhoneNumber(createTestPhoneNumber(TEST_CONTACT_SYNC_ENTITY_01_PHONE_NUMBER_02_LOOKUP_KEY, TEST_CONTACT_SYNC_ENTITY_01_PHONE_NUMBER_02, TEST_CONTACT_SYNC_ENTITY_01_PHONE_NUMBER_TYPE_02));
    updatedTestEntity01.addPhoneNumber(createTestPhoneNumber(TEST_CONTACT_SYNC_ENTITY_01_PHONE_NUMBER_03_LOOKUP_KEY, TEST_CONTACT_SYNC_ENTITY_01_PHONE_NUMBER_03, TEST_CONTACT_SYNC_ENTITY_01_PHONE_NUMBER_TYPE_03));
    testEntities.add(updatedTestEntity01);


    mockSynchronizeEntitiesWithDevice(testEntities);


    Assert.assertEquals(1, entityManager.getAllEntitiesOfType(ContactSyncEntity.class).size());
    Assert.assertEquals(2, entityManager.getAllEntitiesOfType(PhoneNumberSyncEntity.class).size());
    Assert.assertEquals(0, entityManager.getAllEntitiesOfType(EmailSyncEntity.class).size());
    Assert.assertEquals(2, entityManager.getAllEntitiesOfType(SyncJobItem.class).size());
    Assert.assertEquals(3, entityManager.getAllEntitiesOfType(SyncEntityLocalLookupKeys.class).size());

    ContactSyncEntity updatedContact = entityManager.getAllEntitiesOfType(ContactSyncEntity.class).get(0);

    List<PhoneNumberSyncEntity> updatedPhoneNumbers = new ArrayList<>(updatedContact.getPhoneNumbers());

    Assert.assertEquals(2, updatedPhoneNumbers.size());
    Assert.assertFalse(updatedPhoneNumbers.contains(phoneNumberToDelete));
    Assert.assertTrue(updatedPhoneNumbers.contains(updatedTestEntity01.getPhoneNumbers().get(0)));
    Assert.assertTrue(updatedPhoneNumbers.contains(updatedTestEntity01.getPhoneNumbers().get(1)));

    List<EmailSyncEntity> updatedEmails = new ArrayList<>(updatedContact.getEmailAddresses());

    Assert.assertEquals(0, updatedEmails.size());
    Assert.assertFalse(updatedEmails.contains(emailToDelete));
  }



  protected PhoneNumberSyncEntity createTestPhoneNumber(String lookupKey, String phoneNumber, PhoneNumberType phoneNumberType) {
    PhoneNumberSyncEntity entity = new PhoneNumberSyncEntity(phoneNumber, phoneNumberType);

    entity.setLocalLookupKey(lookupKey);

    return entity;
  }

  protected EmailSyncEntity createTestEmail(String lookupKey, String emailAddress, EmailType emailType) {
    EmailSyncEntity entity = new EmailSyncEntity(emailAddress, emailType);

    entity.setLocalLookupKey(lookupKey);

    return entity;
  }


  protected void mockSynchronizeEntityWithDevice(SyncEntity testEntity) {
    List<SyncEntity> testEntities = new ArrayList<>();
    testEntities.add(testEntity);

    mockSynchronizeEntitiesWithDevice(testEntities);
  }

  protected void mockSynchronizeEntitiesWithDevice(List<SyncEntity> testEntities) {
    syncModuleMock.callEntityChangedListeners(testEntities);
  }



  /*        Handling synchronized entities        */

  @Test
  public void sendCreatedEntityToRemote_SyncStateChangesToDone() {
    ContactSyncEntity entity = new ContactSyncEntity();
    entity.setDisplayName(TEST_CONTACT_SYNC_ENTITY_01_DISPLAY_NAME);

    assertThat(getCountOfStoredSyncJobItems(), is(0));


    synchronizeCreatedEntity(entity);


    assertThat(getCountOfStoredSyncJobItems(), is(1));

    SyncJobItem syncJobItem = (SyncJobItem)getAllEntitiesOfType(SyncJobItem.class).get(0);

    assertThat(syncJobItem.getState(), is(SyncState.DONE));
    assertThat(syncJobItem.getFinishTime(), notNullValue());
  }

  @Test
  public void sendCreatedEntityToRemote_LookupKeyGetsCreated() {
    ContactSyncEntity entity = new ContactSyncEntity();
    entity.setDisplayName(TEST_CONTACT_SYNC_ENTITY_01_DISPLAY_NAME);

    assertThat(getCountOfStoredSyncEntityLocalLookupKeys(), is(0));


    synchronizeCreatedEntity(entity);


    assertThat(getCountOfStoredSyncEntityLocalLookupKeys(), is(1));

    SyncEntityLocalLookupKeys lookupKey = (SyncEntityLocalLookupKeys)getAllEntitiesOfType(SyncEntityLocalLookupKeys.class).get(0);

    assertThat(lookupKey.getEntityLastModifiedOnDevice(), notNullValue());
  }

  @Test
  public void sendCreatedEntityWithSyncPropertiesToRemote_LookupKeysGetCreated() {
    ContactSyncEntity entity = new ContactSyncEntity();
    entity.setDisplayName(TEST_CONTACT_SYNC_ENTITY_01_DISPLAY_NAME);

    PhoneNumberSyncEntity phoneNumber01 = createTestPhoneNumber(TEST_CONTACT_SYNC_ENTITY_01_PHONE_NUMBER_01_LOOKUP_KEY, TEST_CONTACT_SYNC_ENTITY_01_PHONE_NUMBER_01, TEST_CONTACT_SYNC_ENTITY_01_PHONE_NUMBER_TYPE_01);
    entityManager.persistEntity(phoneNumber01);
    entity.addPhoneNumber(phoneNumber01);
    EmailSyncEntity email01 = createTestEmail(TEST_CONTACT_SYNC_ENTITY_01_EMAIL_01_LOOKUP_KEY, TEST_CONTACT_SYNC_ENTITY_01_EMAIL_ADDRESS_01, TEST_CONTACT_SYNC_ENTITY_01_EMAIL_TYPE_01);
    entityManager.persistEntity(email01);
    entity.addEmailAddress(email01);

    assertThat(getCountOfStoredSyncEntityLocalLookupKeys(), is(0));


    synchronizeCreatedEntity(entity);


    assertThat(getCountOfStoredSyncEntityLocalLookupKeys(), is(3));

    for(SyncEntityLocalLookupKeys lookupKey : (List<SyncEntityLocalLookupKeys>)getAllEntitiesOfType(SyncEntityLocalLookupKeys.class)) {
      assertThat(lookupKey.getEntityLastModifiedOnDevice(), notNullValue());
    }
  }


  @Test
  public void sendUpdatedEntityToRemote_SyncStateChangesToDone() {
    ContactSyncEntity entity = new ContactSyncEntity();
    entity.setDisplayName(TEST_CONTACT_SYNC_ENTITY_01_DISPLAY_NAME);
    mockSynchronizeEntityWithDevice(entity);

    assertThat(getCountOfStoredSyncJobItems(), is(1));
    SyncJobItem entityCreatedSyncJob = (SyncJobItem)getAllEntitiesOfType(SyncJobItem.class).get(0);


    entity.setDisplayName(TEST_UPDATED_CONTACT_SYNC_ENTITY_01_DISPLAY_NAME);

    synchronizeEntity(entity);


    assertThat(getCountOfStoredSyncJobItems(), is(2));

    for(SyncJobItem syncJobItem : (List<SyncJobItem>)getAllEntitiesOfType(SyncJobItem.class)) {
      if(syncJobItem != entityCreatedSyncJob) {
        assertThat(syncJobItem.getState(), is(SyncState.DONE));
        assertThat(syncJobItem.getFinishTime(), notNullValue());
      }
    }
  }

  @Test
  public void sendUpdatedEntityToRemote_LookupKeyGetsUpdated() {
    ContactSyncEntity entity = new ContactSyncEntity();
    entity.setDisplayName(TEST_CONTACT_SYNC_ENTITY_01_DISPLAY_NAME);
    mockSynchronizeEntityWithDevice(entity);

    assertThat(getCountOfStoredSyncEntityLocalLookupKeys(), is(1));
    Date lookupKeyCreationDate = ((SyncEntityLocalLookupKeys)getAllEntitiesOfType(SyncEntityLocalLookupKeys.class).get(0)).getEntityLastModifiedOnDevice();


    entity.setDisplayName(TEST_UPDATED_CONTACT_SYNC_ENTITY_01_DISPLAY_NAME);

    synchronizeEntity(entity);


    assertThat(getCountOfStoredSyncEntityLocalLookupKeys(), is(1));

    SyncEntityLocalLookupKeys lookupKey = (SyncEntityLocalLookupKeys)getAllEntitiesOfType(SyncEntityLocalLookupKeys.class).get(0);

    assertThat(lookupKey.getEntityLastModifiedOnDevice(), notNullValue());
    assertThat(lookupKey.getEntityLastModifiedOnDevice(), is(not(lookupKeyCreationDate)));
  }

  @Test
  public void sendUpdatedEntityWithSyncPropertiesToRemote_LookupKeysGetUpdated() {
    ContactSyncEntity entity = new ContactSyncEntity();
    entity.setDisplayName(TEST_CONTACT_SYNC_ENTITY_01_DISPLAY_NAME);
    mockSynchronizeEntityWithDevice(entity);

    assertThat(getCountOfStoredSyncEntityLocalLookupKeys(), is(1));
    Date lookupKeyCreationDate = ((SyncEntityLocalLookupKeys)getAllEntitiesOfType(SyncEntityLocalLookupKeys.class).get(0)).getEntityLastModifiedOnDevice();


    PhoneNumberSyncEntity phoneNumber01 = createTestPhoneNumber(TEST_CONTACT_SYNC_ENTITY_01_PHONE_NUMBER_01_LOOKUP_KEY, TEST_CONTACT_SYNC_ENTITY_01_PHONE_NUMBER_01, TEST_CONTACT_SYNC_ENTITY_01_PHONE_NUMBER_TYPE_01);
    entityManager.persistEntity(phoneNumber01);
    entity.addPhoneNumber(phoneNumber01);
    EmailSyncEntity email01 = createTestEmail(TEST_CONTACT_SYNC_ENTITY_01_EMAIL_01_LOOKUP_KEY, TEST_CONTACT_SYNC_ENTITY_01_EMAIL_ADDRESS_01, TEST_CONTACT_SYNC_ENTITY_01_EMAIL_TYPE_01);
    entityManager.persistEntity(email01);
    entity.addEmailAddress(email01);

    synchronizeEntity(entity);


    assertThat(getCountOfStoredSyncEntityLocalLookupKeys(), is(3));

    for(SyncEntityLocalLookupKeys lookupKey : (List<SyncEntityLocalLookupKeys>)getAllEntitiesOfType(SyncEntityLocalLookupKeys.class)) {
      assertThat(lookupKey.getEntityLastModifiedOnDevice(), notNullValue());
      assertThat(lookupKey.getEntityLastModifiedOnDevice(), is(not(lookupKeyCreationDate)));
    }
  }


  @Test
  public void sendDeletedEntityToRemote_SyncStateChangesToDone() {
    ContactSyncEntity entity = new ContactSyncEntity();
    entity.setDisplayName(TEST_CONTACT_SYNC_ENTITY_01_DISPLAY_NAME);
    mockSynchronizeEntityWithDevice(entity);

    assertThat(getCountOfStoredSyncJobItems(), is(1));
    SyncJobItem entityCreatedSyncJob = (SyncJobItem)getAllEntitiesOfType(SyncJobItem.class).get(0);


    // TODO: how do we simulate a deleted entity?
    entity.setDeleted(true);
    synchronizeDeletedEntity(entity);


    assertThat(getCountOfStoredSyncJobItems(), is(2));

    for(SyncJobItem syncJobItem : (List<SyncJobItem>)getAllEntitiesOfType(SyncJobItem.class)) {
      if(syncJobItem != entityCreatedSyncJob) {
        assertThat(syncJobItem.getState(), is(SyncState.DONE));
        assertThat(syncJobItem.getFinishTime(), notNullValue());
      }
    }
  }

  @Test
  public void sendDeletedEntityToRemote_LookupKeyGetsDeleted() {
    ContactSyncEntity entity = new ContactSyncEntity();
    entity.setDisplayName(TEST_CONTACT_SYNC_ENTITY_01_DISPLAY_NAME);
    mockSynchronizeEntityWithDevice(entity);

    assertThat(getCountOfStoredSyncEntityLocalLookupKeys(), is(1));
    SyncEntityLocalLookupKeys lookupKeyBefore = (SyncEntityLocalLookupKeys)getAllEntitiesOfType(SyncEntityLocalLookupKeys.class).get(0);


    // TODO: how do we simulate a deleted entity?
    entity.setDeleted(true);
    synchronizeDeletedEntity(entity);


    assertThat(getCountOfStoredSyncEntityLocalLookupKeys(), is(0));

    assertThat(lookupKeyBefore.isDeleted(), is(true));
  }

  @Test
  public void sendDeletedEntityWithSyncPropertiesToRemote_LookupKeysGetDeleted() {
    ContactSyncEntity entity = new ContactSyncEntity();
    entity.setDisplayName(TEST_CONTACT_SYNC_ENTITY_01_DISPLAY_NAME);

    PhoneNumberSyncEntity phoneNumber01 = createTestPhoneNumber(TEST_CONTACT_SYNC_ENTITY_01_PHONE_NUMBER_01_LOOKUP_KEY, TEST_CONTACT_SYNC_ENTITY_01_PHONE_NUMBER_01, TEST_CONTACT_SYNC_ENTITY_01_PHONE_NUMBER_TYPE_01);
    entityManager.persistEntity(phoneNumber01);
    entity.addPhoneNumber(phoneNumber01);
    EmailSyncEntity email01 = createTestEmail(TEST_CONTACT_SYNC_ENTITY_01_EMAIL_01_LOOKUP_KEY, TEST_CONTACT_SYNC_ENTITY_01_EMAIL_ADDRESS_01, TEST_CONTACT_SYNC_ENTITY_01_EMAIL_TYPE_01);
    entityManager.persistEntity(email01);
    entity.addEmailAddress(email01);

    mockSynchronizeEntityWithDevice(entity);

    assertThat(getCountOfStoredSyncEntityLocalLookupKeys(), is(3));
    List<SyncEntityLocalLookupKeys> lookupKeysBefore = (List<SyncEntityLocalLookupKeys>)getAllEntitiesOfType(SyncEntityLocalLookupKeys.class);


    // TODO: how do we simulate a deleted entity?
    entity.setDeleted(true);
    phoneNumber01.setDeleted(true);
    email01.setDeleted(true);
    synchronizeDeletedEntity(entity);


    assertThat(getCountOfStoredSyncEntityLocalLookupKeys(), is(0));

    for(SyncEntityLocalLookupKeys lookupKey : lookupKeysBefore) {
      assertThat(lookupKey.isDeleted(), is(true));
    }
  }

  @Test
  public void sendDeletedSyncPropertiesToRemote_LookupKeysGetDeleted() {
    ContactSyncEntity entity = new ContactSyncEntity();
    entity.setDisplayName(TEST_CONTACT_SYNC_ENTITY_01_DISPLAY_NAME);

    PhoneNumberSyncEntity phoneNumber01 = createTestPhoneNumber(TEST_CONTACT_SYNC_ENTITY_01_PHONE_NUMBER_01_LOOKUP_KEY, TEST_CONTACT_SYNC_ENTITY_01_PHONE_NUMBER_01, TEST_CONTACT_SYNC_ENTITY_01_PHONE_NUMBER_TYPE_01);
    entityManager.persistEntity(phoneNumber01);
    entity.addPhoneNumber(phoneNumber01);
    PhoneNumberSyncEntity phoneNumber02 = createTestPhoneNumber(TEST_CONTACT_SYNC_ENTITY_01_PHONE_NUMBER_02_LOOKUP_KEY, TEST_CONTACT_SYNC_ENTITY_01_PHONE_NUMBER_02, TEST_CONTACT_SYNC_ENTITY_01_PHONE_NUMBER_TYPE_02);
    entityManager.persistEntity(phoneNumber02);
    entity.addPhoneNumber(phoneNumber02);
    PhoneNumberSyncEntity phoneNumber03 = createTestPhoneNumber(TEST_CONTACT_SYNC_ENTITY_01_PHONE_NUMBER_03_LOOKUP_KEY, TEST_CONTACT_SYNC_ENTITY_01_PHONE_NUMBER_03, TEST_CONTACT_SYNC_ENTITY_01_PHONE_NUMBER_TYPE_03);
    entityManager.persistEntity(phoneNumber03);
    entity.addPhoneNumber(phoneNumber03);
    EmailSyncEntity email01 = createTestEmail(TEST_CONTACT_SYNC_ENTITY_01_EMAIL_01_LOOKUP_KEY, TEST_CONTACT_SYNC_ENTITY_01_EMAIL_ADDRESS_01, TEST_CONTACT_SYNC_ENTITY_01_EMAIL_TYPE_01);
    entityManager.persistEntity(email01);
    entity.addEmailAddress(email01);

    mockSynchronizeEntityWithDevice(entity);

    assertThat(getCountOfStoredSyncEntityLocalLookupKeys(), is(5));


    // TODO: how do we simulate a deleted entity?
    entity.removePhoneNumber(phoneNumber02);
    entity.removePhoneNumber(phoneNumber03);
    entity.removeEmailAddress(email01);
    phoneNumber02.setDeleted(true);
    phoneNumber03.setDeleted(true);
    email01.setDeleted(true);

    synchronizeDeletedEntity(entity);
    // TODO: deleted properties currently aren't detected!
    synchronizeDeletedEntity(phoneNumber02);
    synchronizeDeletedEntity(phoneNumber03);
    synchronizeDeletedEntity(email01);


    assertThat(getCountOfStoredSyncEntityLocalLookupKeys(), is(2));
  }


  @Test
  public void sendCreatedFileEntityToRemote_SyncStateChangesToTransferring() {
    FileSyncEntity entity = new FileSyncEntity();
    entity.setFilePath(TEST_FILE_SYNC_ENTITY_01_PATH);

    assertThat(getCountOfStoredSyncJobItems(), is(0));


    synchronizeCreatedFileEntity(entity);


    assertThat(getCountOfStoredSyncJobItems(), is(1));

    SyncJobItem syncJobItem = (SyncJobItem)getAllEntitiesOfType(SyncJobItem.class).get(0);

    assertThat(syncJobItem.getState(), is(SyncState.TRANSFERRING_FILE_TO_DESTINATION_DEVICE));
    assertThat(syncJobItem.getFinishTime(), nullValue());
  }


  @Test
  public void createFileLocally_RemoteWantsToStartSynchronization_FileSenderGetsCalled() {
    FileSyncEntity entity = new FileSyncEntity();
    entity.setFilePath(TEST_FILE_SYNC_ENTITY_01_PATH);

    mockSynchronizeEntityWithDevice(entity);

    verifyZeroInteractions(fileSender);
    assertThat(getCountOfStoredSyncJobItems(), is(1));
    SyncJobItem syncJobItem = (SyncJobItem)getAllEntitiesOfType(SyncJobItem.class).get(0);


    syncJobItem.setState(SyncState.TRANSFERRING_FILE_TO_DESTINATION_DEVICE);

    mockSendSyncJobItemFromRemoteToLocalDevice(syncJobItem);


    assertThat(getCountOfStoredSyncJobItems(), is(1));

    verify(fileSender, times(1)).sendFileAsync(any(FileSyncJobItem.class));
  }

  @Test
  public void createFileLocally_RemoteDoesNotWantToStartSynchronization_FileSenderDoesNotGetCalled() {
    FileSyncEntity entity = new FileSyncEntity();
    entity.setFilePath(TEST_FILE_SYNC_ENTITY_01_PATH);

    mockSynchronizeEntityWithDevice(entity);

    verifyZeroInteractions(fileSender);
    assertThat(getCountOfStoredSyncJobItems(), is(1));
    SyncJobItem syncJobItem = (SyncJobItem)getAllEntitiesOfType(SyncJobItem.class).get(0);


    syncJobItem.setState(SyncState.DONE);

    mockSendSyncJobItemFromRemoteToLocalDevice(syncJobItem);


    assertThat(getCountOfStoredSyncJobItems(), is(1));

    verifyZeroInteractions(fileSender);
  }

  @Test
  public void createFileOnRemote_FileSenderDoesNotGetCalled() {
    FileSyncEntity entity = new FileSyncEntity();
    entity.setFilePath(TEST_FILE_SYNC_ENTITY_01_PATH);

    synchronizeCreatedEntity(entity);

    assertThat(getCountOfStoredSyncJobItems(), is(1));
    SyncJobItem syncJobItem = (SyncJobItem)getAllEntitiesOfType(SyncJobItem.class).get(0);


    syncJobItem.setState(SyncState.TRANSFERRING_FILE_TO_DESTINATION_DEVICE); // a bit constructed, should never be that way

    mockSendSyncJobItemFromRemoteToLocalDevice(syncJobItem);


    verifyZeroInteractions(fileSender);
  }


  protected SyncJobItem synchronizeCreatedEntity(SyncEntity entity) {
    entityManager.persistEntity(entity);

    return synchronizeEntity(entity);
  }

  protected SyncJobItem synchronizeDeletedEntity(SyncEntity entity) {
//    entityManager.deleteEntity(entity);

    return synchronizeEntity(entity);
  }

  protected SyncJobItem synchronizeEntity(SyncEntity entity) {
    SyncJobItem syncJobItem = new SyncJobItem(syncModuleConfiguration, entity, remoteDevice, localConfig.getLocalDevice());
    entityManager.persistEntity(syncJobItem);

    mockSendSyncJobItemFromRemoteToLocalDevice(syncJobItem);

    return syncJobItem;
  }

  protected void mockSendSyncJobItemFromRemoteToLocalDevice(BaseEntity entity) {
    registeredSynchronizationListener.entitySynchronized(entity);
  }


  protected SyncJobItem synchronizeCreatedFileEntity(FileSyncEntity entity) {
    entityManager.persistEntity(entity);

    return synchronizeFileEntity(entity);
  }

  protected SyncJobItem synchronizeDeletedFileEntity(FileSyncEntity entity) {
//    entityManager.deleteEntity(entity);

    return synchronizeFileEntity(entity);
  }

  protected SyncJobItem synchronizeFileEntity(FileSyncEntity entity) {
    SyncJobItem syncJobItem = new SyncJobItem(fileSyncModuleConfiguration, entity, remoteDevice, localConfig.getLocalDevice());
    entityManager.persistEntity(syncJobItem);

    registeredSynchronizationListener.entitySynchronized(syncJobItem);

    return syncJobItem;
  }


  /*      Update SyncConfiguration      */

  @Test
  public void disableSyncModuleConfiguration_RemoteDeactivatesSyncModule() {
    assertThat(underTest.activatedSyncModules.size(), is(2));
    assertThat(underTest.activatedSyncModules.containsKey(fileSyncModule), is(true));


    fileSyncModuleConfiguration.setEnabled(false);
    entityManager.updateEntity(fileSyncModuleConfiguration);

    mockSendSyncJobItemFromRemoteToLocalDevice(syncConfiguration);


    assertThat(underTest.activatedSyncModules.size(), is(1));
    assertThat(underTest.activatedSyncModules.containsKey(fileSyncModule), is(false));
  }

  @Test
  public void disableSyncModuleConfiguration_RemoteRemovesSyncModuleListener() {
    assertThat(fileSyncModulesEntityChangeListeners.size(), is(1));


    fileSyncModuleConfiguration.setEnabled(false);
    entityManager.updateEntity(fileSyncModuleConfiguration);

    mockSendSyncJobItemFromRemoteToLocalDevice(syncConfiguration);


    assertThat(fileSyncModulesEntityChangeListeners.size(), is(0));
  }


  @Test
  public void enableSyncModuleConfiguration_RemoteActivatesSyncModule() {
    fileSyncModuleConfiguration.setEnabled(false);
    entityManager.updateEntity(fileSyncModuleConfiguration);

    mockSendSyncJobItemFromRemoteToLocalDevice(syncConfiguration);

    assertThat(underTest.activatedSyncModules.size(), is(1));
    assertThat(underTest.activatedSyncModules.containsKey(fileSyncModule), is(false));


    fileSyncModuleConfiguration.setEnabled(true);
    entityManager.updateEntity(fileSyncModuleConfiguration);

    mockSendSyncJobItemFromRemoteToLocalDevice(syncConfiguration);


    assertThat(underTest.activatedSyncModules.size(), is(2));
    assertThat(underTest.activatedSyncModules.containsKey(fileSyncModule), is(true));
  }

  @Test
  public void enableSyncModuleConfiguration_RemoteAddsSyncModuleListener() {
    fileSyncModuleConfiguration.setEnabled(false);
    entityManager.updateEntity(fileSyncModuleConfiguration);

    mockSendSyncJobItemFromRemoteToLocalDevice(syncConfiguration);

    assertThat(fileSyncModulesEntityChangeListeners.size(), is(0));


    fileSyncModuleConfiguration.setEnabled(true);
    entityManager.updateEntity(fileSyncModuleConfiguration);

    mockSendSyncJobItemFromRemoteToLocalDevice(syncConfiguration);


    assertThat(fileSyncModulesEntityChangeListeners.size(), is(1));
  }


  @Test
  public void deleteSyncConfiguration_SynchronizationWithRemoteGetsStopped() {
    verify(devicesManager, times(0)).stopSynchronizingWithDevice(discoveredRemoteDevice);

    syncConfiguration.setDeleted(true);


    mockSendSyncJobItemFromRemoteToLocalDevice(syncConfiguration);


    verify(devicesManager, times(1)).stopSynchronizingWithDevice(discoveredRemoteDevice);
  }

  @Test
  public void deleteSyncConfiguration_SyncModulesGetStopped() {
    assertThat(underTest.activatedSyncModules.size(), is(2));
    assertThat(fileSyncModulesEntityChangeListeners.size(), is(1));

    syncConfiguration.setDeleted(true);


    mockSendSyncJobItemFromRemoteToLocalDevice(syncConfiguration);


    assertThat(underTest.activatedSyncModules.size(), is(0));
    assertThat(fileSyncModulesEntityChangeListeners.size(), is(0));
  }


  @Test
  public void addSyncConfiguration_SynchronizationWithRemoteGetsStarted() {
    syncConfiguration.setDeleted(true);
    mockSendSyncJobItemFromRemoteToLocalDevice(syncConfiguration);

    verify(devicesManager, times(0)).remoteDeviceStartedSynchronizingWithUs(remoteDevice);


    List<SyncModuleConfiguration> newSyncModuleConfigurations = Arrays.asList(new SyncModuleConfiguration[] { syncModuleConfiguration, fileSyncModuleConfiguration });
    SyncConfiguration newSyncConfiguration = new SyncConfiguration(remoteDevice, localConfig.getLocalDevice(), newSyncModuleConfigurations);
    entityManager.persistEntity(newSyncConfiguration);

    mockSendSyncJobItemFromRemoteToLocalDevice(newSyncConfiguration);


    verify(devicesManager, times(1)).remoteDeviceStartedSynchronizingWithUs(remoteDevice);
  }


  protected int getCountOfStoredSyncJobItems() {
    return getCountOfStoredEntities(SyncJobItem.class);
  }

  protected int getCountOfStoredSyncEntityLocalLookupKeys() {
    return getCountOfStoredEntities(SyncEntityLocalLookupKeys.class);
  }

  protected int getCountOfStoredContacts() {
    return getCountOfStoredEntities(ContactSyncEntity.class);
  }

  protected int getCountOfStoredEntities(Class<? extends BaseEntity> entityClass) {
    return getAllEntitiesOfType(entityClass).size();
  }

  protected List<? extends BaseEntity> getAllEntitiesOfType(Class<? extends BaseEntity> entityClass) {
    return entityManager.getAllEntitiesOfType(entityClass);
  }

}
