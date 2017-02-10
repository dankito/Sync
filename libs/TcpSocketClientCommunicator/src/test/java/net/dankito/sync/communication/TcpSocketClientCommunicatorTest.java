package net.dankito.sync.communication;

import com.couchbase.lite.support.Base64;

import net.dankito.sync.Device;
import net.dankito.sync.OsType;
import net.dankito.sync.communication.callbacks.SendRequestCallback;
import net.dankito.sync.communication.message.DeviceInfo;
import net.dankito.sync.communication.message.Request;
import net.dankito.sync.communication.message.Response;
import net.dankito.sync.communication.message.ResponseErrorType;
import net.dankito.sync.devices.DiscoveredDevice;
import net.dankito.sync.devices.INetworkSettings;
import net.dankito.sync.devices.NetworkSettings;
import net.dankito.utils.IThreadPool;
import net.dankito.utils.ObjectHolder;
import net.dankito.utils.ThreadPool;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.matchers.JUnitMatchers.containsString;


public class TcpSocketClientCommunicatorTest {

  protected static final int MESSAGES_RECEIVER_PORT = 54321;

  protected static final String DEVICE_ID = "1";

  protected static final String DEVICE_UNIQUE_ID = "Remote_1";

  protected static final String DEVICE_NAME = "Love";

  protected static final String DEVICE_OS_NAME = "Arch Linux";

  protected static final String DEVICE_OS_VERSION = "4.9";

  protected static final OsType DEVICE_OS_TYPE = OsType.DESKTOP;


  protected IClientCommunicator underTest;

  protected Device remoteDevice;

  protected DiscoveredDevice discoveredRemoteDevice;

  protected RequestReceiver remoteRequestReceiver;

  protected IMessageHandler remoteMessageHandler;

  protected INetworkSettings remoteNetworkSettings;

  protected IThreadPool threadPool;

  protected SocketHandler socketHandler;

  protected IMessageSerializer messageSerializer;


  @Before
  public void setUp() throws Exception {
    socketHandler = Mockito.spy(new SocketHandler());
    messageSerializer = Mockito.spy(new JsonMessageSerializer());
    threadPool = new ThreadPool();

    setupRemoteMessagesReceiver();

    underTest = new TcpSocketClientCommunicator(socketHandler, messageSerializer, threadPool);
  }

  protected void setupRemoteMessagesReceiver() {
    remoteDevice = Mockito.mock(Device.class);
    Mockito.when(remoteDevice.getId()).thenReturn(DEVICE_ID);
    Mockito.when(remoteDevice.getUniqueDeviceId()).thenReturn(DEVICE_UNIQUE_ID);
    Mockito.when(remoteDevice.getName()).thenReturn(DEVICE_NAME);
    Mockito.when(remoteDevice.getOsName()).thenReturn(DEVICE_OS_NAME);
    Mockito.when(remoteDevice.getOsVersion()).thenReturn(DEVICE_OS_VERSION);
    Mockito.when(remoteDevice.getOsType()).thenReturn(DEVICE_OS_TYPE);

    discoveredRemoteDevice = new DiscoveredDevice(remoteDevice, "localhost");

    final CountDownLatch waitForMessagesReceiverBeingSetupLatch = new CountDownLatch(1);

    remoteNetworkSettings = new NetworkSettings();
    remoteNetworkSettings.setLocalHostDevice(remoteDevice);

    remoteMessageHandler = new MessageHandler(remoteNetworkSettings);

    remoteRequestReceiver = Mockito.spy(new RequestReceiver(socketHandler, remoteMessageHandler, messageSerializer, threadPool));
    remoteRequestReceiver.start(MESSAGES_RECEIVER_PORT, new RequestReceiverCallback() {
      @Override
      public void started(IRequestReceiver requestReceiver, boolean couldStartReceiver, int messagesReceiverPort, Exception startException) {
        if(couldStartReceiver) {
          discoveredRemoteDevice.setMessagesPort(messagesReceiverPort);
          waitForMessagesReceiverBeingSetupLatch.countDown();
        }
      }
    });

    try { waitForMessagesReceiverBeingSetupLatch.await(1, TimeUnit.SECONDS); } catch(Exception e) { }
  }

  @After
  public void tearDown() throws Exception {
    remoteRequestReceiver.close();
  }


  @Test
  public void getDeviceInfo() throws Exception {
    final ObjectHolder<Response<DeviceInfo>> responseHolder = new ObjectHolder<>();
    final CountDownLatch countDownLatch = new CountDownLatch(1);

    underTest.getDeviceInfo(discoveredRemoteDevice, new SendRequestCallback<DeviceInfo>() {
      @Override
      public void done(Response<DeviceInfo> response) {
        responseHolder.setObject(response);
        countDownLatch.countDown();
      }
    });

    try { countDownLatch.await(1, TimeUnit.SECONDS); } catch(Exception ignored) { }

    assertThat(responseHolder.isObjectSet(), is(true));

    Response<DeviceInfo> response = responseHolder.getObject();
    assertThat(response.isCouldHandleMessage(), is(true));

    DeviceInfo receivedDeviceInfo = response.getBody();
    assertThat(receivedDeviceInfo.getId(), is(DEVICE_ID));
    assertThat(receivedDeviceInfo.getUniqueDeviceId(), is(DEVICE_UNIQUE_ID));
    assertThat(receivedDeviceInfo.getName(), is(DEVICE_NAME));
    assertThat(receivedDeviceInfo.getOsName(), is(DEVICE_OS_NAME));
    assertThat(receivedDeviceInfo.getOsVersion(), is(DEVICE_OS_VERSION));
    assertThat(receivedDeviceInfo.getOsType(), is(DEVICE_OS_TYPE));
  }


  @Test
  public void sendRequestToClosedClient() throws Exception {
    remoteRequestReceiver.close();

    final ObjectHolder<Response<DeviceInfo>> responseHolder = new ObjectHolder<>();
    final CountDownLatch countDownLatch = new CountDownLatch(1);

    underTest.getDeviceInfo(discoveredRemoteDevice, new SendRequestCallback<DeviceInfo>() {
      @Override
      public void done(Response<DeviceInfo> response) {
        responseHolder.setObject(response);
        countDownLatch.countDown();
      }
    });

    try { countDownLatch.await(1, TimeUnit.SECONDS); } catch(Exception ignored) { }


    assertThatErrorTypeIs(responseHolder, ResponseErrorType.SEND_REQUEST_TO_REMOTE);
  }


  @Test
  public void sendRequest_SerializingRequestFails() throws Exception {
    Mockito.doThrow(Exception.class).when(messageSerializer).serializeRequest(Mockito.any(Request.class));

    final ObjectHolder<Response<DeviceInfo>> responseHolder = new ObjectHolder<>();
    final CountDownLatch countDownLatch = new CountDownLatch(1);

    underTest.getDeviceInfo(discoveredRemoteDevice, new SendRequestCallback<DeviceInfo>() {
      @Override
      public void done(Response<DeviceInfo> response) {
        responseHolder.setObject(response);
        countDownLatch.countDown();
      }
    });

    try { countDownLatch.await(1, TimeUnit.SECONDS); } catch(Exception ignored) { }


    assertThatErrorTypeIs(responseHolder, ResponseErrorType.SERIALIZE_REQUEST);
  }


  @Test
  public void sendRequest_DeserializingResponseFails() throws Exception {
    Mockito.doReturn(new Response(ResponseErrorType.DESERIALIZE_RESPONSE, new Exception())).when(messageSerializer).deserializeResponse(Mockito.anyString(), Mockito.anyString());

    final ObjectHolder<Response<DeviceInfo>> responseHolder = new ObjectHolder<>();
    final CountDownLatch countDownLatch = new CountDownLatch(1);

    underTest.getDeviceInfo(discoveredRemoteDevice, new SendRequestCallback<DeviceInfo>() {
      @Override
      public void done(Response<DeviceInfo> response) {
        responseHolder.setObject(response);
        countDownLatch.countDown();
      }
    });

    try { countDownLatch.await(1, TimeUnit.SECONDS); } catch(Exception ignored) { }


    assertThatErrorTypeIs(responseHolder, ResponseErrorType.DESERIALIZE_RESPONSE);
  }


  @Test
  public void sendRequest_RetrievingResponseFails() throws Exception {
    Mockito.when(socketHandler.receiveMessage(Mockito.any(Socket.class)))
        .thenCallRealMethod()
        .thenReturn(new SocketResult(false));

    final ObjectHolder<Response<DeviceInfo>> responseHolder = new ObjectHolder<>();
    final CountDownLatch countDownLatch = new CountDownLatch(1);

    underTest.getDeviceInfo(discoveredRemoteDevice, new SendRequestCallback<DeviceInfo>() {
      @Override
      public void done(Response<DeviceInfo> response) {
        responseHolder.setObject(response);
        countDownLatch.countDown();
      }
    });

    try { countDownLatch.await(1, TimeUnit.SECONDS); } catch(Exception ignored) { }


    assertThatErrorTypeIs(responseHolder, ResponseErrorType.RETRIEVE_RESPONSE);
  }


  @Test
  public void deserializingRequestFails() throws Exception {
    Mockito.doThrow(Exception.class).when(messageSerializer).deserializeRequest(Mockito.anyString());

    final ObjectHolder<Response<DeviceInfo>> responseHolder = new ObjectHolder<>();
    final CountDownLatch countDownLatch = new CountDownLatch(1);

    underTest.getDeviceInfo(discoveredRemoteDevice, new SendRequestCallback<DeviceInfo>() {
      @Override
      public void done(Response<DeviceInfo> response) {
        responseHolder.setObject(response);
        countDownLatch.countDown();
      }
    });

    try { countDownLatch.await(1, TimeUnit.SECONDS); } catch(Exception ignored) { }


    assertThatErrorTypeIs(responseHolder, ResponseErrorType.DESERIALIZE_REQUEST);
  }


  @Test
  public void serializeResponseFails() throws Exception {
    Mockito.doThrow(Exception.class).when(messageSerializer).serializeResponse(Mockito.any(Response.class));

    final ObjectHolder<Response<DeviceInfo>> responseHolder = new ObjectHolder<>();
    final CountDownLatch countDownLatch = new CountDownLatch(1);

    underTest.getDeviceInfo(discoveredRemoteDevice, new SendRequestCallback<DeviceInfo>() {
      @Override
      public void done(Response<DeviceInfo> response) {
        responseHolder.setObject(response);
        countDownLatch.countDown();
      }
    });

    try { countDownLatch.await(1, TimeUnit.SECONDS); } catch(Exception ignored) { }


    assertThatErrorTypeIs(responseHolder, ResponseErrorType.RETRIEVE_RESPONSE);
  }


  @Test
  public void sendMessageReturnsError() {
    Mockito.doReturn(new SocketResult(false)).when(socketHandler).sendMessage(Mockito.any(Socket.class), Mockito.any(byte[].class));

    final ObjectHolder<Response<DeviceInfo>> responseHolder = new ObjectHolder<>();
    final CountDownLatch countDownLatch = new CountDownLatch(1);

    underTest.getDeviceInfo(discoveredRemoteDevice, new SendRequestCallback<DeviceInfo>() {
      @Override
      public void done(Response<DeviceInfo> response) {
        responseHolder.setObject(response);
        countDownLatch.countDown();
      }
    });

    try { countDownLatch.await(1, TimeUnit.SECONDS); } catch(Exception ignored) { }


    assertThatErrorTypeIs(responseHolder, ResponseErrorType.SEND_REQUEST_TO_REMOTE);
  }


  @Test
  public void sendMessageThatExceedsMaxMessageSize() {
    StringBuffer stringThatExceedsMaxMessageSize = new StringBuffer(CommunicationConfig.MAX_MESSAGE_SIZE + 1);
    for(int i = 0; i <= CommunicationConfig.MAX_MESSAGE_SIZE; i++) {
      stringThatExceedsMaxMessageSize.append('a');
    }

    Mockito.when(remoteDevice.getName()).thenReturn(stringThatExceedsMaxMessageSize.toString());

    final ObjectHolder<Response<DeviceInfo>> responseHolder = new ObjectHolder<>();
    final CountDownLatch countDownLatch = new CountDownLatch(1);

    underTest.getDeviceInfo(discoveredRemoteDevice, new SendRequestCallback<DeviceInfo>() {
      @Override
      public void done(Response<DeviceInfo> response) {
        responseHolder.setObject(response);
        countDownLatch.countDown();
      }
    });

    try { countDownLatch.await(1, TimeUnit.SECONDS); } catch(Exception ignored) { }

    assertThat(responseHolder.isObjectSet(), is(true));

    Response<DeviceInfo> response = responseHolder.getObject();
    assertThat(response.isCouldHandleMessage(), is(false));
    assertThat(response.getError().getMessage(), containsString("exceeds max message length"));
  }


  @Test
  public void sendMessageFails_ExceptionIsPassedOnToCallback() throws IOException {
    IOException exceptionToReturn = new IOException("Arbitrary Exception");
    Mockito.doThrow(exceptionToReturn).when(socketHandler).sendMessage(Mockito.any(Base64.InputStream.class), Mockito.any(OutputStream.class));

    final ObjectHolder<Response<DeviceInfo>> responseHolder = new ObjectHolder<>();
    final CountDownLatch countDownLatch = new CountDownLatch(1);

    underTest.getDeviceInfo(discoveredRemoteDevice, new SendRequestCallback<DeviceInfo>() {
      @Override
      public void done(Response<DeviceInfo> response) {
        responseHolder.setObject(response);
        countDownLatch.countDown();
      }
    });

    try { countDownLatch.await(1, TimeUnit.SECONDS); } catch(Exception ignored) { }


    assertThat(responseHolder.isObjectSet(), is(true));

    Response<DeviceInfo> response = responseHolder.getObject();
    assertThat(response.isCouldHandleMessage(), is(false));
    assertThat(exceptionToReturn, is(response.getError()));
  }


  @Test
  public void receivedRequestAsyncThrowsException_SimplyToHave100PercentLineCoverage() {
    Mockito.doThrow(Exception.class).when(remoteRequestReceiver).receivedRequestAsync(Mockito.any(Socket.class));

    final ObjectHolder<Response<DeviceInfo>> responseHolder = new ObjectHolder<>();
    final CountDownLatch countDownLatch = new CountDownLatch(1);

    underTest.getDeviceInfo(discoveredRemoteDevice, new SendRequestCallback<DeviceInfo>() {
      @Override
      public void done(Response<DeviceInfo> response) {
        responseHolder.setObject(response);
        countDownLatch.countDown();
      }
    });

    try { countDownLatch.await(1, TimeUnit.SECONDS); } catch(Exception ignored) { }


    assertThat(responseHolder.isObjectSet(), is(false));
  }


  protected void assertThatErrorTypeIs(ObjectHolder<Response<DeviceInfo>> responseHolder, ResponseErrorType errorType) {
    assertThat(responseHolder.isObjectSet(), is(true));

    Response<DeviceInfo> response = responseHolder.getObject();

    assertThat(response.isCouldHandleMessage(), is(false));
    assertThat(response.getErrorType(), is(errorType));
  }

}