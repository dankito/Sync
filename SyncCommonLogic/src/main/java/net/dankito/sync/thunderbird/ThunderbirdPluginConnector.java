package net.dankito.sync.thunderbird;


import net.dankito.communication.IMessageHandler;
import net.dankito.communication.IMessageSerializer;
import net.dankito.communication.IRequestReceiver;
import net.dankito.communication.IRequestSender;
import net.dankito.communication.JsonMessageSerializer;
import net.dankito.communication.RequestReceiver;
import net.dankito.communication.RequestReceiverCallback;
import net.dankito.communication.RequestSender;
import net.dankito.communication.SocketHandler;
import net.dankito.communication.callback.SendRequestCallback;
import net.dankito.communication.message.Request;
import net.dankito.communication.message.Response;
import net.dankito.communication.message.ResponseErrorType;
import net.dankito.sync.ContactSyncEntity;
import net.dankito.sync.EmailSyncEntity;
import net.dankito.sync.EmailType;
import net.dankito.sync.SyncEntityState;
import net.dankito.sync.devices.DiscoveredDevice;
import net.dankito.sync.synchronization.SyncEntityChange;
import net.dankito.sync.synchronization.SyncEntityChangeListener;
import net.dankito.sync.thunderbird.callback.ContactSynchronizedListener;
import net.dankito.sync.thunderbird.callback.ThunderbirdCallback;
import net.dankito.sync.thunderbird.model.ContactSync;
import net.dankito.sync.thunderbird.model.ThunderbirdContact;
import net.dankito.sync.thunderbird.response.GetAddressBookResponse;
import net.dankito.utils.IThreadPool;
import net.dankito.utils.StringUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class ThunderbirdPluginConnector {

  private static final Logger log = LoggerFactory.getLogger(ThunderbirdPluginConnector.class);
  protected static final String PRIMARY_EMAIL_LOOKUP_KEY_SUFFIX = "_PrimaryEmail";
  protected static final String SECOND_EMAIL_LOOKUP_KEY_SUFFIX = "_SecondEmail";


  protected DiscoveredDevice discoveredThunderbird;

  protected IRequestSender requestSender;

  protected IRequestReceiver requestReceiver;

  protected SyncEntityChangeListener syncEntityChangeListener;

  protected SocketAddress thunderbirdAddress;

  protected int messagesReceiverPort;


  public ThunderbirdPluginConnector(DiscoveredDevice thunderbird, IThreadPool threadPool, SyncEntityChangeListener syncEntityChangeListener) {
    this.discoveredThunderbird = thunderbird;
    this.syncEntityChangeListener = syncEntityChangeListener;

    this.thunderbirdAddress = new InetSocketAddress(thunderbird.getAddress(), thunderbird.getMessagesPort());

    // TODO: this is in large parts a copy of TcpSocketClientCommunicator

    SocketHandler socketHandler = new SocketHandler();
    IMessageHandler messageHandler = new ThunderbirdMessageHandler(contactSynchronizedListener);
    IMessageSerializer messageSerializer = new JsonMessageSerializer(messageHandler);

    this.requestSender = new RequestSender(socketHandler, messageSerializer, threadPool);

    startRequestReceiver(socketHandler, messageHandler, messageSerializer, threadPool);
  }

  protected void startRequestReceiver(SocketHandler socketHandler, IMessageHandler messageHandler, IMessageSerializer messageSerializer, IThreadPool threadPool) {
    this.requestReceiver = new RequestReceiver(socketHandler, messageHandler, messageSerializer, threadPool);

    requestReceiver.start(ThunderbirdPluginConnectorConfig.DEFAULT_RECEIVER_PORT, new RequestReceiverCallback() {
      @Override
      public void started(IRequestReceiver requestReceiver, boolean couldStartReceiver, int messagesReceiverPort, Exception startException) {
        if(couldStartReceiver) {
          ThunderbirdPluginConnector.this.messagesReceiverPort = messagesReceiverPort;
          // TODO: tell Thunderbird messages receiver port
        }
      }
    });
  }


  public void getAddressBookAsync(final ThunderbirdCallback<GetAddressBookResponse> callback) {
    requestSender.sendRequestAndReceiveResponseAsync(thunderbirdAddress, new Request(ThunderbirdMessageConfig.GET_ADDRESS_BOOK_MESSAGE), new SendRequestCallback() {
      @Override
      public void done(Response response) {
        if(response.isCouldHandleMessage() == false) {
          callback.done(new GetAddressBookResponse(mapResponseErrorType(response.getErrorType()), response.getError()));
        }
        else {
          mapGetAddressBookResponse(response, callback);
        }
      }
    });
  }

  protected void mapGetAddressBookResponse(Response response, ThunderbirdCallback<GetAddressBookResponse> callback) {
    try {
      List<ContactSyncEntity> contacts = new ArrayList<>();
      ThunderbirdContact[] thunderbirdContacts = (ThunderbirdContact[]) response.getBody();

      for (ThunderbirdContact thunderbirdContact : thunderbirdContacts) {
        contacts.add(mapThunderbirdContact(thunderbirdContact));
      }

      callback.done(new GetAddressBookResponse(contacts));
    } catch(Exception e) {
      log.error("Could not map Thunderbird Contacts to ContactSyncEntities", e);
      callback.done(new GetAddressBookResponse(net.dankito.sync.communication.message.ResponseErrorType.DESERIALIZE_RESPONSE, e));
    }
  }


  public void syncContact(ContactSyncEntity entity, SyncEntityState state, final ThunderbirdCallback<net.dankito.sync.communication.message.Response<ThunderbirdContact>> callback) {
    ContactSync body = new ContactSync(mapContactSyncEntity(entity), state);

    requestSender.sendRequestAndReceiveResponseAsync(thunderbirdAddress, new Request(ThunderbirdMessageConfig.SYNC_CONTACT_MESSAGE, body), new SendRequestCallback() {
      @Override
      public void done(Response response) {
        if(response.isCouldHandleMessage() == false) {
          callback.done(new net.dankito.sync.communication.message.Response<ThunderbirdContact>(mapResponseErrorType(response.getErrorType()), response.getError()));
        }
        else {
          callback.done(new net.dankito.sync.communication.message.Response<ThunderbirdContact>((ThunderbirdContact)response.getBody()));
        }
      }
    });
  }


  protected void contactSynchronized(ThunderbirdContact contact, SyncEntityState state) {
    ContactSyncEntity mappedContact = mapThunderbirdContact(contact);

    entitySynchronized(mappedContact, state);
  }

  protected void entitySynchronized(ContactSyncEntity contact, SyncEntityState state) {
    syncEntityChangeListener.entityChanged(new SyncEntityChange(null, contact, state, discoveredThunderbird));
  }


  protected ContactSyncEntity mapThunderbirdContact(ThunderbirdContact thunderbirdContact) {
    ContactSyncEntity contact = new ContactSyncEntity();

    contact.setLocalLookupKey(thunderbirdContact.uuid);
    contact.setLastModifiedOnDevice(new Date(thunderbirdContact.LastModifiedDate));

    contact.setDisplayName(thunderbirdContact.DisplayName);
    contact.setGivenName(thunderbirdContact.FirstName);
    contact.setFamilyName(thunderbirdContact.LastName);
    contact.setNickname(thunderbirdContact.NickName);

    // TODO: add remaining

    if(StringUtils.isNotNullOrEmpty(thunderbirdContact.PrimaryEmail)) {
      EmailSyncEntity primaryEmail = new EmailSyncEntity(thunderbirdContact.PrimaryEmail, EmailType.OTHER);
      primaryEmail.setLocalLookupKey(contact.getLocalLookupKey() + PRIMARY_EMAIL_LOOKUP_KEY_SUFFIX); // Thunderbird has no lookup key for Emails -> create own one
      contact.addEmailAddress(primaryEmail);
    }
    if(StringUtils.isNotNullOrEmpty(thunderbirdContact.SecondEmail)) {
      EmailSyncEntity secondEmail = new EmailSyncEntity(thunderbirdContact.SecondEmail, EmailType.OTHER);
      secondEmail.setLocalLookupKey(contact.getLocalLookupKey() + SECOND_EMAIL_LOOKUP_KEY_SUFFIX); // Thunderbird has no lookup key for Emails -> create own one
      contact.addEmailAddress(secondEmail);
    }

    return contact;
  }

  protected ThunderbirdContact mapContactSyncEntity(ContactSyncEntity contact) {
    ThunderbirdContact mapped = new ThunderbirdContact();

    mapped.DisplayName = contact.getDisplayName();
    mapped.FirstName = contact.getGivenName();
    mapped.LastName = contact.getFamilyName();
    mapped.NickName = contact.getNickname();

    if(contact.getEmailAddresses().size() > 0) {
      mapped.PrimaryEmail = contact.getEmailAddresses().get(0).getAddress(); // TODO: Android's Email type gets lost in this way
    }
    if(contact.getEmailAddresses().size() > 1) {
      mapped.SecondEmail = contact.getEmailAddresses().get(1).getAddress(); // TODO: Android's Email type gets lost in this way
    }

    // TODO: add remaining

    return mapped;
  }


  // TODO: this is a copy of same method in TcpSocketClientCommunicator
  protected net.dankito.sync.communication.message.ResponseErrorType mapResponseErrorType(ResponseErrorType errorType) {
    if(errorType != null) {
      return net.dankito.sync.communication.message.ResponseErrorType.valueOf(errorType.name());
    }

    return null;
  }


  protected ContactSynchronizedListener contactSynchronizedListener = new ContactSynchronizedListener() {
    @Override
    public void contactSynchronized(ThunderbirdContact contact, SyncEntityState state) {
      ThunderbirdPluginConnector.this.contactSynchronized(contact, state);
    }
  };

}
