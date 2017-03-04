package net.dankito.sync.synchronization;


import net.dankito.sync.BaseEntity;
import net.dankito.sync.ContactSyncEntity;
import net.dankito.sync.Device;
import net.dankito.sync.EmailSyncEntity;
import net.dankito.sync.EmailType;
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
import net.dankito.sync.persistence.CouchbaseLiteEntityManagerBase;
import net.dankito.sync.persistence.CouchbaseLiteEntityManagerJava;
import net.dankito.sync.persistence.EntityManagerConfiguration;
import net.dankito.sync.persistence.IEntityManager;
import net.dankito.sync.synchronization.merge.IDataMerger;
import net.dankito.sync.synchronization.merge.JpaMetadataBasedDataMerger;
import net.dankito.sync.synchronization.modules.ISyncModule;
import net.dankito.sync.synchronization.util.SyncConfigurationManagerStub;
import net.dankito.sync.synchronization.util.SyncModuleMock;
import net.dankito.utils.ObjectHolder;
import net.dankito.utils.ThreadPool;
import net.dankito.utils.services.JavaFileStorageService;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;

public class SyncConfigurationManagerBaseTest {

  protected static final String TEST_CONTACT_SYNC_ENTITY_01_LOCAL_ID = "01";
  protected static final String TEST_CONTACT_SYNC_ENTITY_01_DISPLAY_NAME = "Mandela";

  protected static final String TEST_CONTACT_SYNC_ENTITY_01_PHONE_NUMBER_01 = "0123456789";
  protected static final String TEST_UPDATED_CONTACT_SYNC_ENTITY_01_PHONE_NUMBER_01 = "01234567890";
  protected static final PhoneNumberType TEST_CONTACT_SYNC_ENTITY_01_PHONE_NUMBER_TYPE_01 = PhoneNumberType.MOBILE;
  protected static final PhoneNumberType TEST_UPDATED_CONTACT_SYNC_ENTITY_01_PHONE_NUMBER_TYPE_01 = PhoneNumberType.WORK;
  protected static final String TEST_CONTACT_SYNC_ENTITY_01_PHONE_NUMBER_02 = "03456789129";
  protected static final PhoneNumberType TEST_CONTACT_SYNC_ENTITY_01_PHONE_NUMBER_TYPE_02 = PhoneNumberType.HOME;
  protected static final String TEST_CONTACT_SYNC_ENTITY_01_PHONE_NUMBER_03 = "0789123456";
  protected static final PhoneNumberType TEST_CONTACT_SYNC_ENTITY_01_PHONE_NUMBER_TYPE_03 = PhoneNumberType.OTHER;

  protected static final String TEST_CONTACT_SYNC_ENTITY_01_EMAIL_ADDRESS_01 = "nelson@heroes.net";
  protected static final String TEST_UPDATED_CONTACT_SYNC_ENTITY_01_EMAIL_ADDRESS_01 = "nelson@greatest-heroes.net";
  protected static final EmailType TEST_CONTACT_SYNC_ENTITY_01_EMAIL_TYPE_01 = EmailType.WORK;
  protected static final EmailType TEST_UPDATED_CONTACT_SYNC_ENTITY_01_EMAIL_TYPE_01 = EmailType.HOME;

  protected static final String TEST_CONTACT_SYNC_ENTITY_02_LOCAL_ID = "02";
  protected static final String TEST_CONTACT_SYNC_ENTITY_02_DISPLAY_NAME = "Gandhi";

  protected static final String TEST_CONTACT_SYNC_ENTITY_02_PHONE_NUMBER_01 = "0987654321";
  protected static final PhoneNumberType TEST_CONTACT_SYNC_ENTITY_02_PHONE_NUMBER_TYPE_01 = PhoneNumberType.WORK;

  protected static final String TEST_CONTACT_SYNC_ENTITY_02_EMAIL_ADDRESS_01 = "mahatma@heroes.net";
  protected static final EmailType TEST_CONTACT_SYNC_ENTITY_02_EMAIL_TYPE_01 = EmailType.WORK;
  protected static final String TEST_CONTACT_SYNC_ENTITY_02_EMAIL_ADDRESS_02 = "private@gandhi.net";
  protected static final EmailType TEST_CONTACT_SYNC_ENTITY_02_EMAIL_TYPE_02 = EmailType.HOME;
  protected static final String TEST_CONTACT_SYNC_ENTITY_02_EMAIL_ADDRESS_03 = "free-india@british-empire.co.uk";
  protected static final EmailType TEST_CONTACT_SYNC_ENTITY_02_EMAIL_TYPE_03 = EmailType.OTHER;


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

  protected SynchronizationListener registeredSynchronizationListener;


  @Before
  public void setUp() throws Exception {
    syncManager = Mockito.mock(ISyncManager.class);
    doAnswer(new Answer() {
      @Override
      public Object answer(InvocationOnMock invocation) throws Throwable {
        registeredSynchronizationListener = (SynchronizationListener)invocation.getArguments()[0];
        return null;
      }
    }).when(syncManager).addSynchronizationListener(any(SynchronizationListener.class));

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
    testEntity01.addPhoneNumber(createTestPhoneNumber("1.1", TEST_CONTACT_SYNC_ENTITY_01_PHONE_NUMBER_01, TEST_CONTACT_SYNC_ENTITY_01_PHONE_NUMBER_TYPE_01));
    testEntity01.addPhoneNumber(createTestPhoneNumber("1.2", TEST_CONTACT_SYNC_ENTITY_01_PHONE_NUMBER_02, TEST_CONTACT_SYNC_ENTITY_01_PHONE_NUMBER_TYPE_02));
    testEntity01.addPhoneNumber(createTestPhoneNumber("1.3", TEST_CONTACT_SYNC_ENTITY_01_PHONE_NUMBER_03, TEST_CONTACT_SYNC_ENTITY_01_PHONE_NUMBER_TYPE_03));
    testEntities.add(testEntity01);

    ContactSyncEntity testEntity02 = new ContactSyncEntity();
    testEntity02.setLocalLookupKey(TEST_CONTACT_SYNC_ENTITY_02_LOCAL_ID);
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
  public void syncNewEntitiesWithEmailSyncEntityProperties() {
    List<SyncEntity> testEntities = new ArrayList<>();

    ContactSyncEntity testEntity01 = new ContactSyncEntity();
    testEntity01.setLocalLookupKey(TEST_CONTACT_SYNC_ENTITY_01_LOCAL_ID);
    testEntity01.setDisplayName(TEST_CONTACT_SYNC_ENTITY_01_DISPLAY_NAME);
    testEntity01.addEmailAddress(createTestEmail("1.1", TEST_CONTACT_SYNC_ENTITY_01_EMAIL_ADDRESS_01, TEST_CONTACT_SYNC_ENTITY_01_EMAIL_TYPE_01));
    testEntities.add(testEntity01);

    ContactSyncEntity testEntity02 = new ContactSyncEntity();
    testEntity02.setLocalLookupKey(TEST_CONTACT_SYNC_ENTITY_02_LOCAL_ID);
    testEntity02.setDisplayName(TEST_CONTACT_SYNC_ENTITY_02_DISPLAY_NAME);
    testEntity02.addEmailAddress(createTestEmail("2.1", TEST_CONTACT_SYNC_ENTITY_02_EMAIL_ADDRESS_01, TEST_CONTACT_SYNC_ENTITY_02_EMAIL_TYPE_01));
    testEntity02.addEmailAddress(createTestEmail("2.2", TEST_CONTACT_SYNC_ENTITY_02_EMAIL_ADDRESS_02, TEST_CONTACT_SYNC_ENTITY_02_EMAIL_TYPE_02));
    testEntity02.addEmailAddress(createTestEmail("2.3", TEST_CONTACT_SYNC_ENTITY_02_EMAIL_ADDRESS_03, TEST_CONTACT_SYNC_ENTITY_02_EMAIL_TYPE_03));
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
    updatedTestEntity01.addEmailAddress(createTestEmail("1.1", TEST_CONTACT_SYNC_ENTITY_01_EMAIL_ADDRESS_01, TEST_CONTACT_SYNC_ENTITY_01_EMAIL_TYPE_01));
    updatedTestEntity01.setLastModifiedOnDevice(new Date());
    testEntities.add(updatedTestEntity01);

    ContactSyncEntity updatedTestEntity02 = new ContactSyncEntity();
    updatedTestEntity02.setLocalLookupKey(TEST_CONTACT_SYNC_ENTITY_02_LOCAL_ID);
    updatedTestEntity02.setDisplayName(TEST_CONTACT_SYNC_ENTITY_02_DISPLAY_NAME);
    updatedTestEntity02.addEmailAddress(createTestEmail("1.2", TEST_CONTACT_SYNC_ENTITY_02_EMAIL_ADDRESS_02, TEST_CONTACT_SYNC_ENTITY_02_EMAIL_TYPE_02));
    updatedTestEntity02.addEmailAddress(createTestEmail("1.3", TEST_CONTACT_SYNC_ENTITY_02_EMAIL_ADDRESS_03, TEST_CONTACT_SYNC_ENTITY_02_EMAIL_TYPE_03));
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
    testEntity01.addPhoneNumber(createTestPhoneNumber("1.1", TEST_CONTACT_SYNC_ENTITY_01_PHONE_NUMBER_01, TEST_CONTACT_SYNC_ENTITY_01_PHONE_NUMBER_TYPE_01));
    testEntity01.addEmailAddress(createTestEmail("1.1", TEST_CONTACT_SYNC_ENTITY_01_EMAIL_ADDRESS_01, TEST_CONTACT_SYNC_ENTITY_01_EMAIL_TYPE_01));
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
    updatedTestEntity01.addPhoneNumber(createTestPhoneNumber("1.1", TEST_UPDATED_CONTACT_SYNC_ENTITY_01_PHONE_NUMBER_01, TEST_UPDATED_CONTACT_SYNC_ENTITY_01_PHONE_NUMBER_TYPE_01));
    updatedTestEntity01.addEmailAddress(createTestEmail("1.1", TEST_UPDATED_CONTACT_SYNC_ENTITY_01_EMAIL_ADDRESS_01, TEST_UPDATED_CONTACT_SYNC_ENTITY_01_EMAIL_TYPE_01));
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
    PhoneNumberSyncEntity phoneNumberToDelete = createTestPhoneNumber("1.1", TEST_CONTACT_SYNC_ENTITY_01_PHONE_NUMBER_01, TEST_CONTACT_SYNC_ENTITY_01_PHONE_NUMBER_TYPE_01);
    EmailSyncEntity emailToDelete = createTestEmail("1.1", TEST_CONTACT_SYNC_ENTITY_01_EMAIL_ADDRESS_01, TEST_CONTACT_SYNC_ENTITY_01_EMAIL_TYPE_01);
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
    updatedTestEntity01.addPhoneNumber(createTestPhoneNumber("1.2", TEST_CONTACT_SYNC_ENTITY_01_PHONE_NUMBER_02, TEST_CONTACT_SYNC_ENTITY_01_PHONE_NUMBER_TYPE_02));
    updatedTestEntity01.addPhoneNumber(createTestPhoneNumber("1.3", TEST_CONTACT_SYNC_ENTITY_01_PHONE_NUMBER_03, TEST_CONTACT_SYNC_ENTITY_01_PHONE_NUMBER_TYPE_03));
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


  protected void mockSynchronizeEntitiesWithDevice(final List<SyncEntity> testEntities) {
    syncModuleMock.callEntityChangedListeners(testEntities);
  }



  /*        Handling synchronized entities        */

  @Test
  public void sendEntityToRemote_SyncStateChanges() {
    ContactSyncEntity entity = new ContactSyncEntity();
    entity.setDisplayName(TEST_CONTACT_SYNC_ENTITY_01_DISPLAY_NAME);

    assertThat(getCountOfStoredSyncJobItems(), is(0));


    synchronizeEntity(entity);


    assertThat(getCountOfStoredSyncJobItems(), is(1));

    SyncJobItem syncJobItem = (SyncJobItem)getAllEntitiesOfType(SyncJobItem.class).get(0);

    assertThat(syncJobItem.getState(), is(SyncState.DONE));
    assertThat(syncJobItem.getFinishTime(), notNullValue());
  }

  @Test
  public void sendEntityToRemote_LookupKeyGetsCreated() {
    ContactSyncEntity entity = new ContactSyncEntity();
    entity.setDisplayName(TEST_CONTACT_SYNC_ENTITY_01_DISPLAY_NAME);

    assertThat(getCountOfStoredSyncEntityLocalLookupKeys(), is(0));


    synchronizeEntity(entity);


    assertThat(getCountOfStoredSyncEntityLocalLookupKeys(), is(1));

    SyncEntityLocalLookupKeys lookupKey = (SyncEntityLocalLookupKeys)getAllEntitiesOfType(SyncEntityLocalLookupKeys.class).get(0);

    assertThat(lookupKey.getEntityLastModifiedOnDevice(), notNullValue());
  }


  protected SyncJobItem synchronizeEntity(SyncEntity entity) {
    entityManager.persistEntity(entity);

    SyncJobItem syncJobItem = new SyncJobItem(syncModuleConfiguration, entity, remoteDevice, localConfig.getLocalDevice());
    entityManager.persistEntity(syncJobItem);

    registeredSynchronizationListener.entitySynchronized(syncJobItem);

    return syncJobItem;
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
