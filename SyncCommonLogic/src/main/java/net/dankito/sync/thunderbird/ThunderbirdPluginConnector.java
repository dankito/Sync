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
import net.dankito.sync.devices.DiscoveredDevice;
import net.dankito.sync.thunderbird.callback.ThunderbirdCallback;
import net.dankito.sync.thunderbird.model.ThunderbirdContact;
import net.dankito.sync.thunderbird.response.GetAddressBookResponse;
import net.dankito.utils.IThreadPool;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class ThunderbirdPluginConnector {
  
  private static final Logger log = LoggerFactory.getLogger(ThunderbirdPluginConnector.class);



  protected DiscoveredDevice discoveredThunderbird;

  protected IRequestSender requestSender;

  protected IRequestReceiver requestReceiver;

  protected IThreadPool threadPool;

  protected SocketAddress thunderbirdAddress;

  protected int messagesReceiverPort;


  public ThunderbirdPluginConnector(DiscoveredDevice thunderbird, IThreadPool threadPool) {
    this.discoveredThunderbird = thunderbird;

    this.thunderbirdAddress = new InetSocketAddress(thunderbird.getAddress(), thunderbird.getMessagesPort());

    // TODO: this is in large parts a copy of TcpSocketClientCommunicator

    SocketHandler socketHandler = new SocketHandler();
    IMessageHandler messageHandler = new ThunderbirdMessageHandler();
    IMessageSerializer messageSerializer = new JsonMessageSerializer(messageHandler);

    this.requestSender = new RequestSender(socketHandler, messageSerializer, threadPool);

    startRequestReceiver(socketHandler, messageHandler, messageSerializer);
  }

  protected void startRequestReceiver(SocketHandler socketHandler, IMessageHandler messageHandler, IMessageSerializer messageSerializer) {
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

  protected ContactSyncEntity mapThunderbirdContact(ThunderbirdContact thunderbirdContact) {
    ContactSyncEntity contact = new ContactSyncEntity();

    contact.setLocalLookupKey(thunderbirdContact.uuid);
    contact.setLastModifiedOnDevice(new Date(thunderbirdContact.LastModifiedDate));

    contact.setDisplayName(thunderbirdContact.DisplayName);
    contact.setGivenName(thunderbirdContact.FirstName);
    contact.setFamilyName(thunderbirdContact.LastName);
    contact.setNickname(thunderbirdContact.NickName);

    // TODO: add remaining

    if(thunderbirdContact.PrimaryEmail != null) {
      contact.addEmailAddress(new EmailSyncEntity(thunderbirdContact.PrimaryEmail, EmailType.OTHER));
    }

    return contact;
  }

  // TODO: this is a copy of same method in TcpSocketClientCommunicator
  protected net.dankito.sync.communication.message.ResponseErrorType mapResponseErrorType(ResponseErrorType errorType) {
    if(errorType != null) {
      return net.dankito.sync.communication.message.ResponseErrorType.valueOf(errorType.name());
    }

    return null;
  }


}
