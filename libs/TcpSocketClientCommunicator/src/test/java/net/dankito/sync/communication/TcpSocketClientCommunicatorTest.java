package net.dankito.sync.communication;

import net.dankito.sync.Device;
import net.dankito.sync.OsType;
import net.dankito.sync.communication.callbacks.SendRequestCallback;
import net.dankito.sync.communication.message.DeviceInfo;
import net.dankito.sync.communication.message.Request;
import net.dankito.sync.communication.message.Response;
import net.dankito.sync.communication.message.ResponseErrorType;
import net.dankito.sync.devices.DiscoveredDevice;
import net.dankito.sync.devices.INetworkSettings;
import net.dankito.sync.devices.NetworkSetting;
import net.dankito.sync.devices.NetworkSettings;
import net.dankito.sync.devices.NetworkSettingsChangedListener;
import net.dankito.utils.IThreadPool;
import net.dankito.utils.ObjectHolder;
import net.dankito.utils.ThreadPool;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.net.Socket;
import java.util.concurrent.CountDownLatch;


public class TcpSocketClientCommunicatorTest {

  protected static final int MESSAGES_RECEIVER_PORT = 54321;

  protected static final String DEVICE_ID = "1";

  protected static final String DEVICE_UNIQUE_ID = "Remote_1";

  protected static final String DEVICE_NAME = "Love";

  protected static final String DEVICE_OS_NAME = "Arch Linux";

  protected static final String DEVICE_OS_VERSION = "4.9";

  protected static final OsType DEVICE_OS_TYPE = OsType.DESKTOP;


  protected IClientCommunicator underTest;

  protected DiscoveredDevice remoteDevice;

  protected IRequestReceiver remoteRequestReceiver;

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
    Device remoteDev = Mockito.mock(Device.class);
    Mockito.when(remoteDev.getId()).thenReturn(DEVICE_ID);
    Mockito.when(remoteDev.getUniqueDeviceId()).thenReturn(DEVICE_UNIQUE_ID);
    Mockito.when(remoteDev.getName()).thenReturn(DEVICE_NAME);
    Mockito.when(remoteDev.getOsName()).thenReturn(DEVICE_OS_NAME);
    Mockito.when(remoteDev.getOsVersion()).thenReturn(DEVICE_OS_VERSION);
    Mockito.when(remoteDev.getOsType()).thenReturn(DEVICE_OS_TYPE);

    remoteDevice = new DiscoveredDevice(remoteDev, "localhost");

    final CountDownLatch waitForMessagesReceiverBeingSetupLatch = new CountDownLatch(1);

    remoteNetworkSettings = new NetworkSettings();
    remoteNetworkSettings.setLocalHostDevice(remoteDev);
    remoteNetworkSettings.addListener(new NetworkSettingsChangedListener() {
      @Override
      public void settingsChanged(INetworkSettings networkSettings, NetworkSetting setting, Object newValue, Object oldValue) {
        if(setting == NetworkSetting.MESSAGES_PORT) {
          remoteDevice.setMessagesPort((int)newValue);
          waitForMessagesReceiverBeingSetupLatch.countDown();
        }
      }
    });

    remoteMessageHandler = new MessageHandler(remoteNetworkSettings);

    remoteRequestReceiver = new RequestReceiver(socketHandler, remoteMessageHandler, messageSerializer, threadPool);
    remoteRequestReceiver.start(MESSAGES_RECEIVER_PORT, remoteNetworkSettings);

    try { waitForMessagesReceiverBeingSetupLatch.await(); } catch(Exception e) { }
  }

  @After
  public void tearDown() throws Exception {
    remoteRequestReceiver.close();
  }


  @Test
  public void getDeviceInfo() throws Exception {
    final ObjectHolder<Response<DeviceInfo>> responseHolder = new ObjectHolder<>();
    final CountDownLatch countDownLatch = new CountDownLatch(1);

    underTest.getDeviceInfo(remoteDevice, new SendRequestCallback<DeviceInfo>() {
      @Override
      public void done(Response<DeviceInfo> response) {
        responseHolder.setObject(response);
        countDownLatch.countDown();
      }
    });

    try { countDownLatch.await(); } catch(Exception ignored) { }

    Assert.assertTrue(responseHolder.isObjectSet());

    Response<DeviceInfo> response = responseHolder.getObject();
    Assert.assertTrue(response.isCouldHandleMessage());

    DeviceInfo receivedDeviceInfo = response.getBody();
    Assert.assertEquals(DEVICE_ID, receivedDeviceInfo.getId());
    Assert.assertEquals(DEVICE_UNIQUE_ID, receivedDeviceInfo.getUniqueDeviceId());
    Assert.assertEquals(DEVICE_NAME, receivedDeviceInfo.getName());
    Assert.assertEquals(DEVICE_OS_NAME, receivedDeviceInfo.getOsName());
    Assert.assertEquals(DEVICE_OS_VERSION, receivedDeviceInfo.getOsVersion());
    Assert.assertEquals(DEVICE_OS_TYPE, receivedDeviceInfo.getOsType());
  }


  @Test
  public void sendRequestToClosedClient() throws Exception {
    remoteRequestReceiver.close();

    final ObjectHolder<Response<DeviceInfo>> responseHolder = new ObjectHolder<>();
    final CountDownLatch countDownLatch = new CountDownLatch(1);

    underTest.getDeviceInfo(remoteDevice, new SendRequestCallback<DeviceInfo>() {
      @Override
      public void done(Response<DeviceInfo> response) {
        responseHolder.setObject(response);
        countDownLatch.countDown();
      }
    });

    try { countDownLatch.await(); } catch(Exception ignored) { }

    Assert.assertTrue(responseHolder.isObjectSet());

    Response<DeviceInfo> response = responseHolder.getObject();
    Assert.assertFalse(response.isCouldHandleMessage());
    Assert.assertEquals(ResponseErrorType.SEND_REQUEST_TO_REMOTE, response.getErrorType());
  }


  @Test
  public void sendRequest_SerializingRequestFails() throws Exception {
    Mockito.doThrow(Exception.class).when(messageSerializer).serializeRequest(Mockito.any(Request.class));

    final ObjectHolder<Response<DeviceInfo>> responseHolder = new ObjectHolder<>();
    final CountDownLatch countDownLatch = new CountDownLatch(1);

    underTest.getDeviceInfo(remoteDevice, new SendRequestCallback<DeviceInfo>() {
      @Override
      public void done(Response<DeviceInfo> response) {
        responseHolder.setObject(response);
        countDownLatch.countDown();
      }
    });

    try { countDownLatch.await(); } catch(Exception ignored) { }

    Assert.assertTrue(responseHolder.isObjectSet());

    Response<DeviceInfo> response = responseHolder.getObject();
    Assert.assertFalse(response.isCouldHandleMessage());
    Assert.assertEquals(ResponseErrorType.SERIALIZE_REQUEST, response.getErrorType());
  }


  @Test
  public void sendRequest_DeserializingResponseFails() throws Exception {
    Mockito.doReturn(new Response(ResponseErrorType.DESERIALIZE_RESPONSE, new Exception())).when(messageSerializer).deserializeResponse(Mockito.anyString(), Mockito.anyString());

    final ObjectHolder<Response<DeviceInfo>> responseHolder = new ObjectHolder<>();
    final CountDownLatch countDownLatch = new CountDownLatch(1);

    underTest.getDeviceInfo(remoteDevice, new SendRequestCallback<DeviceInfo>() {
      @Override
      public void done(Response<DeviceInfo> response) {
        responseHolder.setObject(response);
        countDownLatch.countDown();
      }
    });

    try { countDownLatch.await(); } catch(Exception ignored) { }

    Assert.assertTrue(responseHolder.isObjectSet());

    Response<DeviceInfo> response = responseHolder.getObject();
    Assert.assertFalse(response.isCouldHandleMessage());
    Assert.assertEquals(ResponseErrorType.DESERIALIZE_RESPONSE, response.getErrorType());
  }


  @Test
  public void sendRequest_RetrievingResponseFails() throws Exception {
    Mockito.when(socketHandler.receiveMessage(Mockito.any(Socket.class)))
        .thenCallRealMethod()
        .thenReturn(new SocketResult(false));

    final ObjectHolder<Response<DeviceInfo>> responseHolder = new ObjectHolder<>();
    final CountDownLatch countDownLatch = new CountDownLatch(1);

    underTest.getDeviceInfo(remoteDevice, new SendRequestCallback<DeviceInfo>() {
      @Override
      public void done(Response<DeviceInfo> response) {
        responseHolder.setObject(response);
        countDownLatch.countDown();
      }
    });

    try { countDownLatch.await(); } catch(Exception ignored) { }

    Assert.assertTrue(responseHolder.isObjectSet());

    Response<DeviceInfo> response = responseHolder.getObject();
    Assert.assertFalse(response.isCouldHandleMessage());
    Assert.assertEquals(ResponseErrorType.RETRIEVE_RESPONSE, response.getErrorType());
  }

}