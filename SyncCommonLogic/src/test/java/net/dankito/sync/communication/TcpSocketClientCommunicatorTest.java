package net.dankito.sync.communication;

import net.dankito.sync.Device;
import net.dankito.sync.LocalConfig;
import net.dankito.sync.OsType;
import net.dankito.sync.communication.callback.ClientCommunicatorListener;
import net.dankito.sync.communication.callback.SendRequestCallback;
import net.dankito.sync.communication.message.DeviceInfo;
import net.dankito.sync.communication.message.MessageHandlerConfig;
import net.dankito.sync.communication.message.Response;
import net.dankito.sync.devices.DiscoveredDevice;
import net.dankito.sync.devices.INetworkSettings;
import net.dankito.sync.devices.NetworkSettings;
import net.dankito.utils.ObjectHolder;
import net.dankito.utils.ThreadPool;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;


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

  protected SocketAddress destinationAddress;


  @Before
  public void setUp() throws Exception {
    setUpRemoteDevice();

    INetworkSettings networkSettings = new NetworkSettings(new LocalConfig(remoteDevice));
    MessageHandlerConfig messageHandlerConfig = new MessageHandlerConfig(networkSettings, (IRequestHandler)null);

    underTest = new TcpSocketClientCommunicator(messageHandlerConfig, new ThreadPool());

    final CountDownLatch countDownLatch = new CountDownLatch(1);

    underTest.start(MESSAGES_RECEIVER_PORT, new ClientCommunicatorListener() {
      @Override
      public void started(boolean couldStartMessagesReceiver, int messagesReceiverPort, Exception startException) {
        discoveredRemoteDevice.setMessagesPort(messagesReceiverPort);
        destinationAddress = new InetSocketAddress("localhost", messagesReceiverPort);
        countDownLatch.countDown();
      }
    });

    try { countDownLatch.await(1, TimeUnit.SECONDS); } catch(Exception ignored) { }
  }

  protected void setUpRemoteDevice() {
    remoteDevice = Mockito.mock(Device.class);
    Mockito.when(remoteDevice.getId()).thenReturn(DEVICE_ID);
    Mockito.when(remoteDevice.getUniqueDeviceId()).thenReturn(DEVICE_UNIQUE_ID);
    Mockito.when(remoteDevice.getName()).thenReturn(DEVICE_NAME);
    Mockito.when(remoteDevice.getOsName()).thenReturn(DEVICE_OS_NAME);
    Mockito.when(remoteDevice.getOsVersion()).thenReturn(DEVICE_OS_VERSION);
    Mockito.when(remoteDevice.getOsType()).thenReturn(DEVICE_OS_TYPE);

    discoveredRemoteDevice = new DiscoveredDevice(remoteDevice, "localhost");
  }

  @After
  public void tearDown() throws Exception {

  }

  @Test
  public void getDeviceInfo() throws Exception {
    final ObjectHolder<Response<DeviceInfo>> responseHolder = new ObjectHolder<>();
    final CountDownLatch countDownLatch = new CountDownLatch(1);

    underTest.getDeviceInfo(destinationAddress, new SendRequestCallback<DeviceInfo>() {
      @Override
      public void done(Response<DeviceInfo> response) {
        responseHolder.setObject(response);
        countDownLatch.countDown();
      }
    });


    try { countDownLatch.await(1, TimeUnit.MINUTES); } catch(Exception ignored) { }

    assertThat(responseHolder.isObjectSet(), is(true));

    Response<DeviceInfo> response = responseHolder.getObject();
    assertThat(response.isCouldHandleMessage(), is(true));

    DeviceInfo remoteDeviceInfo = response.getBody();
    assertThat(remoteDeviceInfo, notNullValue());
    assertThat(remoteDeviceInfo.getId(), is(DEVICE_ID));
    assertThat(remoteDeviceInfo.getUniqueDeviceId(), is(DEVICE_UNIQUE_ID));
    assertThat(remoteDeviceInfo.getName(), is(DEVICE_NAME));
    assertThat(remoteDeviceInfo.getOsName(), is(DEVICE_OS_NAME));
    assertThat(remoteDeviceInfo.getOsVersion(), is(DEVICE_OS_VERSION));
    assertThat(remoteDeviceInfo.getOsType(), is(DEVICE_OS_TYPE));
  }

}