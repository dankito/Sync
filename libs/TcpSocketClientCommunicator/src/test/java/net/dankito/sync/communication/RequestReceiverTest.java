package net.dankito.sync.communication;

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

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;


public class RequestReceiverTest {

  protected RequestReceiver underTest;


  protected SocketHandler socketHandler;

  protected IMessageHandler messageHandler;

  protected IMessageSerializer messageSerializer;

  protected IThreadPool threadPool;

  protected INetworkSettings networkSettings;


  @Before
  public void setUp() throws Exception {
    socketHandler = new SocketHandler();
    networkSettings = new NetworkSettings();
    messageHandler = new MessageHandler(networkSettings);
    messageSerializer = new JsonMessageSerializer();
    threadPool = new ThreadPool();

    underTest = new RequestReceiver(socketHandler, messageHandler, messageSerializer, threadPool);
  }

  @After
  public void tearDown() throws Exception {
    underTest.close();
  }

  @Test
  public void start_DesiredPortOutOfExpectedRange() throws Exception {
    final ObjectHolder<Integer> selectedPortHolder = new ObjectHolder<>();
    final CountDownLatch countDownLatch = new CountDownLatch(1);

    networkSettings.addListener(new NetworkSettingsChangedListener() {
      @Override
      public void settingsChanged(INetworkSettings networkSettings, NetworkSetting setting, Object newValue, Object oldValue) {
        selectedPortHolder.setObject((int)newValue); // should never come to this
        countDownLatch.countDown();
      }
    });


    int portOutOfRange = (int)Math.pow(2, 16);
    underTest.start(portOutOfRange, networkSettings);


    try { countDownLatch.await(1, TimeUnit.SECONDS); } catch(Exception ignored) { }

    Assert.assertFalse(selectedPortHolder.isObjectSet());
  }

  @Test
  public void start_DesiredPortAlreadyBound() throws Exception {
    final CountDownLatch countDownLatchRequestReceiver2 = new CountDownLatch(1);

    networkSettings.addListener(new NetworkSettingsChangedListener() {
      @Override
      public void settingsChanged(INetworkSettings networkSettings, NetworkSetting setting, Object newValue, Object oldValue) {
        if(setting == NetworkSetting.MESSAGES_PORT) {
          countDownLatchRequestReceiver2.countDown();
        }
      }
    });

    int alreadyBoundPort = 54321;
    RequestReceiver requestReceiver2 = new RequestReceiver(socketHandler, messageHandler, messageSerializer, threadPool);
    requestReceiver2.start(alreadyBoundPort, networkSettings);

    try { countDownLatchRequestReceiver2.await(1, TimeUnit.SECONDS); } catch(Exception ignored) { }


    final ObjectHolder<Integer> selectedPortHolder = new ObjectHolder<>();
    final CountDownLatch countDownLatchRequestReceiverUnderTest = new CountDownLatch(1);

    networkSettings.addListener(new NetworkSettingsChangedListener() {
      @Override
      public void settingsChanged(INetworkSettings networkSettings, NetworkSetting setting, Object newValue, Object oldValue) {
        if(setting == NetworkSetting.MESSAGES_PORT) {
          selectedPortHolder.setObject((int) newValue); // should never come to this
          countDownLatchRequestReceiverUnderTest.countDown();
        }
      }
    });

    underTest.start(alreadyBoundPort, networkSettings);


    try { countDownLatchRequestReceiverUnderTest.await(1, TimeUnit.SECONDS); } catch(Exception ignored) { }

    Assert.assertTrue(selectedPortHolder.isObjectSet());
    Assert.assertEquals(alreadyBoundPort + 1, (int)selectedPortHolder.getObject());
  }

}