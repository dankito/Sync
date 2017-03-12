package net.dankito.sync.synchronization.modules;

import net.dankito.sync.ContactSyncEntity;
import net.dankito.sync.Device;
import net.dankito.sync.EmailSyncEntity;
import net.dankito.sync.EmailType;
import net.dankito.sync.LocalConfig;
import net.dankito.sync.OsType;
import net.dankito.sync.PhoneNumberSyncEntity;
import net.dankito.sync.PhoneNumberType;
import net.dankito.sync.SyncEntity;
import net.dankito.sync.SyncEntityState;
import net.dankito.sync.SyncJobItem;
import net.dankito.sync.communication.IClientCommunicator;
import net.dankito.sync.communication.TcpSocketClientCommunicator;
import net.dankito.sync.communication.callback.SendRequestCallback;
import net.dankito.sync.communication.message.RequestPermitSynchronizationResponseBody;
import net.dankito.sync.communication.message.RequestPermitSynchronizationResult;
import net.dankito.sync.communication.message.RespondToSynchronizationPermittingChallengeResponseBody;
import net.dankito.sync.communication.message.RespondToSynchronizationPermittingChallengeResult;
import net.dankito.sync.communication.message.Response;
import net.dankito.sync.devices.DiscoveredDevice;
import net.dankito.sync.devices.NetworkSettings;
import net.dankito.sync.localization.Localization;
import net.dankito.sync.persistence.EntityManagerStub;
import net.dankito.sync.persistence.IEntityManager;
import net.dankito.sync.synchronization.SyncEntityChange;
import net.dankito.sync.synchronization.SyncEntityChangeListener;
import net.dankito.sync.util.services.Java8Base64Service;
import net.dankito.utils.ObjectHolder;
import net.dankito.utils.ThreadPool;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.util.Date;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;


/**
 * These tests are not really meant to be run in CI or that like.
 * They have been used to test Thunderbird Plugin's functionality.
 *
 * They assume that there's a Thunderbird with installed Sync Plugin running and listening on Port 32797,
 * some required manual user interaction.
 */
@Ignore
public class ThunderbirdContactsSyncModuleIntegrationTest {

  protected static final int THUNDERBIRD_MESSAGES_PORT = 32797;

  protected static final String TEST_DISPLAY_NAME = "Love";

  protected static final String TEST_PRIMARY_EMAIL = "me@love.org";
  protected static final String TEST_PRIMARY_EMAIL_LOCAL_LOOKUP_KEY = "EMAIL_77";

  protected static final String TEST_SECOND_EMAIL = "me@love.net";
  protected static final String TEST_SECOND_EMAIL_LOCAL_LOOKUP_KEY = "EMAIL_78";

  protected static final String TEST_HOME_PHONE_NUMBER = "0800-666 666";
  protected static final String TEST_HOME_PHONE_NUMBER_LOCAL_LOOKUP_KEY = "PHONE_NUMBER_77";


  protected ISyncModule underTest;

  protected DiscoveredDevice thunderbird;

  protected IEntityManager entityManager;


  @Before
  public void setUp() throws Exception {
    Device thunderbirdInfo = new Device("Thunderbird", "Thunderbird", "Thunderbird", OsType.THUNDERBIRD, "", "45.1.7", "");
    thunderbird = new DiscoveredDevice(thunderbirdInfo, "127.0.0.1");
    thunderbird.setMessagesPort(THUNDERBIRD_MESSAGES_PORT);

    entityManager = new EntityManagerStub();

    underTest = new ThunderbirdContactsSyncModule(thunderbird, entityManager, mock(Localization.class), new ThreadPool());
  }

  @After
  public void tearDown() {
    ((ThunderbirdContactsSyncModule)underTest).shutdownConnector();
  }


  @Test
  public void readAllEntitiesAsync() throws Exception {
    final ObjectHolder<List<ContactSyncEntity>> resultHolder = new ObjectHolder<>();
    final CountDownLatch countDownLatch = new CountDownLatch(1);

    underTest.readAllEntitiesAsync(new ReadEntitiesCallback() {
      @Override
      public void done(boolean wasSuccessful, List<? extends SyncEntity> entities) {
        resultHolder.setObject((List<ContactSyncEntity>)entities);
        countDownLatch.countDown();
      }
    });

    try { countDownLatch.await(10, TimeUnit.SECONDS); } catch(Exception ignored) { }


    assertThat(resultHolder.isObjectSet(), is(true));
    assertThat(resultHolder.getObject().size(), is(not(0)));
  }

  @Test
  public void contactIsChangedInThunderbird_SyncEntityChangeListenerGetsCalled() {
    final ObjectHolder<SyncEntityChange> resultHolder = new ObjectHolder<>();
    final CountDownLatch countDownLatch = new CountDownLatch(1);

    // must be manually triggered in Thunderbird by adding, changing or deleting a contact
    underTest.addSyncEntityChangeListener(new SyncEntityChangeListener() {
      @Override
      public void entityChanged(SyncEntityChange syncEntityChange) {
        resultHolder.setObject(syncEntityChange);
        countDownLatch.countDown();
      }
    });

    try { countDownLatch.await(1, TimeUnit.MINUTES); } catch(Exception ignored) { }


    assertThat(resultHolder.isObjectSet(), is(true));
    assertThat(resultHolder.getObject().getSyncEntity(), notNullValue());
    assertThat(resultHolder.getObject().getSyncEntity().getLocalLookupKey(), notNullValue());
    assertThat(resultHolder.getObject().getSyncModule(), is(underTest));
    assertThat(resultHolder.getObject().getSourceDevice(), is(thunderbird));
    assertThat(resultHolder.getObject().hasIncrementalChange(), is(true));
  }


  @Test
  public void pushCreatedContactToThunderbird_ContactGetsInserted() {
    List<ContactSyncEntity> allContactsBefore = getAddressBook();

    ContactSyncEntity newContact = createTestContact();
    String contactId = newContact.getId();
    SyncJobItem jobItem = new SyncJobItem(null, newContact, null, thunderbird.getDevice());

    final ObjectHolder<Boolean> resultObjectHolder = new ObjectHolder<>();
    final CountDownLatch countDownLatch = new CountDownLatch(1);


    underTest.handleRetrievedSynchronizedEntityAsync(jobItem, SyncEntityState.CREATED, new HandleRetrievedSynchronizedEntityCallback() {
      @Override
      public void done(HandleRetrievedSynchronizedEntityResult result) {
        resultObjectHolder.setObject(result.isSuccessful());
        countDownLatch.countDown();
      }
    });


    try { countDownLatch.await(3, TimeUnit.MINUTES); } catch(Exception ignored) { }

    assertThat(resultObjectHolder.getObject(), is(true));
    assertThat(newContact.getId(), is(contactId));
    assertThat(newContact.getLocalLookupKey(), notNullValue());
    assertThat(newContact.getLastModifiedOnDevice(), notNullValue());

    assertThat(newContact.getEmailAddresses().size(), is(2));
    assertThat(newContact.getEmailAddresses().get(0).getId(), notNullValue());
    assertThat(newContact.getEmailAddresses().get(0).getLocalLookupKey(), is(TEST_PRIMARY_EMAIL_LOCAL_LOOKUP_KEY));
    assertThat(newContact.getEmailAddresses().get(1).getId(), notNullValue());
    assertThat(newContact.getEmailAddresses().get(1).getLocalLookupKey(), is(TEST_SECOND_EMAIL_LOCAL_LOOKUP_KEY));

//    assertThat(newContact.getPhoneNumbers().size(), is(1));
//    assertThat(newContact.getPhoneNumbers().get(0).getId(), notNullValue());
//    assertThat(newContact.getPhoneNumbers().get(0).getLocalLookupKey(), is(TEST_HOME_PHONE_NUMBER_LOCAL_LOOKUP_KEY));

    List<ContactSyncEntity> allContactsAfterwards = getAddressBook();

    assertThat(allContactsAfterwards.size(), is(allContactsBefore.size() + 1));
  }

  @Test
  public void pushEditedContactToThunderbird_ContactGetsUpdated() {
    List<ContactSyncEntity> allContactsBefore = getAddressBook();

    ContactSyncEntity editedContact = allContactsBefore.get(0);
    editedContact.setDisplayName(TEST_DISPLAY_NAME);
    editedContact.addEmailAddress(new EmailSyncEntity(TEST_PRIMARY_EMAIL, EmailType.HOME));
    editedContact.addEmailAddress(new EmailSyncEntity(TEST_SECOND_EMAIL, EmailType.WORK));
    editedContact.addPhoneNumber(new PhoneNumberSyncEntity(TEST_HOME_PHONE_NUMBER, PhoneNumberType.HOME));

    SyncJobItem jobItem = new SyncJobItem(null, editedContact, null, thunderbird.getDevice());

    String lookupKeyBefore = editedContact.getLocalLookupKey();
    Date lastModifiedOnDeviceBefore = editedContact.getLastModifiedOnDevice();

    final ObjectHolder<Boolean> resultObjectHolder = new ObjectHolder<>();
    final CountDownLatch countDownLatch = new CountDownLatch(1);


    underTest.handleRetrievedSynchronizedEntityAsync(jobItem, SyncEntityState.CHANGED, new HandleRetrievedSynchronizedEntityCallback() {
      @Override
      public void done(HandleRetrievedSynchronizedEntityResult result) {
        resultObjectHolder.setObject(result.isSuccessful());
        countDownLatch.countDown();
      }
    });


    try { countDownLatch.await(3, TimeUnit.SECONDS); } catch(Exception ignored) { }

    assertThat(resultObjectHolder.getObject(), is(true));
    assertThat(editedContact.getLocalLookupKey(), is(lookupKeyBefore));
    assertThat(editedContact.getLastModifiedOnDevice(), is(not(lastModifiedOnDeviceBefore)));

    List<ContactSyncEntity> allContactsAfterwards = getAddressBook();

    assertThat(allContactsAfterwards.size(), is(allContactsBefore.size()));
  }

  @Test
  public void pushDeletedContactToThunderbird_ContactGetsDeleted() {
    ContactSyncEntity contactToBeDeleted = createTestContact();

    insertContact(contactToBeDeleted);

    assertThat(contactToBeDeleted.isDeleted(), is(false));


    List<ContactSyncEntity> allContactsBefore = getAddressBook();

    SyncJobItem jobItem = new SyncJobItem(null, contactToBeDeleted, null, thunderbird.getDevice());

    String lookupKeyBefore = contactToBeDeleted.getLocalLookupKey();

    final ObjectHolder<Boolean> resultObjectHolder = new ObjectHolder<>();
    final CountDownLatch countDownLatch = new CountDownLatch(1);


    underTest.handleRetrievedSynchronizedEntityAsync(jobItem, SyncEntityState.DELETED, new HandleRetrievedSynchronizedEntityCallback() {
      @Override
      public void done(HandleRetrievedSynchronizedEntityResult result) {
        resultObjectHolder.setObject(result.isSuccessful());
        countDownLatch.countDown();
      }
    });


    try { countDownLatch.await(3, TimeUnit.SECONDS); } catch(Exception ignored) { }

    assertThat(resultObjectHolder.getObject(), is(true));
    assertThat(contactToBeDeleted.getLocalLookupKey(), is(lookupKeyBefore));

    List<ContactSyncEntity> allContactsAfterwards = getAddressBook();

    assertThat(allContactsAfterwards.size(), is(allContactsBefore.size() - 1));
  }


  @Test
  public void sendRequestToPermitSynchronization() {
    Device localDevice = new Device("test");
    localDevice.setOsType(OsType.DESKTOP);
    localDevice.setOsName("Linux");
    localDevice.setOsVersion("4.9.12");

    IClientCommunicator clientCommunicator = new TcpSocketClientCommunicator(new NetworkSettings(new LocalConfig(localDevice)), null, new Java8Base64Service(), new ThreadPool());


    final ObjectHolder<Response<RequestPermitSynchronizationResponseBody>> requestPermitSynchronizationHolder = new ObjectHolder<>();
    final CountDownLatch requestPermitSynchronizationLatch = new CountDownLatch(1);

    clientCommunicator.requestPermitSynchronization(thunderbird, new SendRequestCallback<RequestPermitSynchronizationResponseBody>() {
      @Override
      public void done(Response<RequestPermitSynchronizationResponseBody> response) {
        requestPermitSynchronizationHolder.setObject(response);
        requestPermitSynchronizationLatch.countDown();
      }
    });

    try { requestPermitSynchronizationLatch.await(30, TimeUnit.SECONDS); } catch(Exception ignored) { }


    assertThat(requestPermitSynchronizationHolder.isObjectSet(), is(true));
    assertThat(requestPermitSynchronizationHolder.getObject().isCouldHandleMessage(), is(true));
    assertThat(requestPermitSynchronizationHolder.getObject().getBody().getResult(), is(RequestPermitSynchronizationResult.RESPOND_TO_CHALLENGE));


    String nonce = requestPermitSynchronizationHolder.getObject().getBody().getNonce();
    String enteredCode = "111111"; // has to be manually entered in debugger, cannot read from debugger's console
    final ObjectHolder<Response<RespondToSynchronizationPermittingChallengeResponseBody>> respondToSynchronizationPermittingHolder = new ObjectHolder();
    final CountDownLatch respondToSynchronizationPermittingLatch = new CountDownLatch(1);

    clientCommunicator.respondToSynchronizationPermittingChallenge(thunderbird, nonce, enteredCode, new SendRequestCallback<RespondToSynchronizationPermittingChallengeResponseBody>() {
      @Override
      public void done(Response<RespondToSynchronizationPermittingChallengeResponseBody> response) {
        respondToSynchronizationPermittingHolder.setObject(response);
        respondToSynchronizationPermittingLatch.countDown();
      }
    });

    try { respondToSynchronizationPermittingLatch.await(3, TimeUnit.SECONDS); } catch(Exception ignored) { }


    assertThat(respondToSynchronizationPermittingHolder.isObjectSet(), is(true));
    assertThat(respondToSynchronizationPermittingHolder.getObject().isCouldHandleMessage(), is(true));
    assertThat(respondToSynchronizationPermittingHolder.getObject().getBody().getResult(), is(RespondToSynchronizationPermittingChallengeResult.ALLOWED));
    assertThat(respondToSynchronizationPermittingHolder.getObject().getBody().getSynchronizationPort(), is(DiscoveredDevice.DEVICE_DOES_NOT_SUPPORT_ACTIVE_SYNCHRONIZATION));
  }


  protected List<ContactSyncEntity> getAddressBook() {
    final ObjectHolder<List<ContactSyncEntity>> addressBookHolder = new ObjectHolder<>();
    final CountDownLatch countDownLatch = new CountDownLatch(1);

    underTest.readAllEntitiesAsync(new ReadEntitiesCallback() {
      @Override
      public void done(boolean wasSuccessful, List<? extends SyncEntity> entities) {
        addressBookHolder.setObject((List<ContactSyncEntity>)entities);
        countDownLatch.countDown();
      }
    });

    try { countDownLatch.await(3, TimeUnit.SECONDS); } catch(Exception ignored) { }

    return addressBookHolder.getObject();
  }

  protected void insertContact(ContactSyncEntity contactToBeDeleted) {
    final ObjectHolder<Boolean> insertResult = new ObjectHolder<>();
    final CountDownLatch insertCountDownLatch = new CountDownLatch(1);

    underTest.handleRetrievedSynchronizedEntityAsync(new SyncJobItem(null, contactToBeDeleted, null, thunderbird.getDevice()), SyncEntityState.CREATED, new HandleRetrievedSynchronizedEntityCallback() {
      @Override
      public void done(HandleRetrievedSynchronizedEntityResult result) {
        insertResult.setObject(result.isSuccessful());
        insertCountDownLatch.countDown();
      }
    });

    try { insertCountDownLatch.await(3, TimeUnit.SECONDS); } catch(Exception ignored) { }
  }

  protected ContactSyncEntity createTestContact() {
    ContactSyncEntity newContact = new ContactSyncEntity();
    entityManager.persistEntity(newContact);

    newContact.setDisplayName(TEST_DISPLAY_NAME);

    EmailSyncEntity primaryEmail = new EmailSyncEntity(TEST_PRIMARY_EMAIL, EmailType.HOME);
    primaryEmail.setLocalLookupKey(TEST_PRIMARY_EMAIL_LOCAL_LOOKUP_KEY);
    newContact.addEmailAddress(primaryEmail);
    entityManager.persistEntity(primaryEmail);

    EmailSyncEntity secondEmail = new EmailSyncEntity(TEST_SECOND_EMAIL, EmailType.WORK);
    secondEmail.setLocalLookupKey(TEST_SECOND_EMAIL_LOCAL_LOOKUP_KEY);
    newContact.addEmailAddress(secondEmail);
    entityManager.persistEntity(secondEmail);

    PhoneNumberSyncEntity homePhoneNumber = new PhoneNumberSyncEntity(TEST_HOME_PHONE_NUMBER, PhoneNumberType.HOME);
    homePhoneNumber.setLocalLookupKey(TEST_HOME_PHONE_NUMBER_LOCAL_LOOKUP_KEY);
    newContact.addPhoneNumber(homePhoneNumber);
    entityManager.persistEntity(homePhoneNumber);

    return newContact;
  }

}