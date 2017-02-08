package net.dankito.sync.communication;

import net.dankito.sync.devices.INetworkSettings;
import net.dankito.sync.devices.NetworkSetting;
import net.dankito.sync.devices.NetworkSettings;
import net.dankito.sync.devices.NetworkSettingsChangedListener;
import net.dankito.utils.IThreadPool;
import net.dankito.utils.ObjectHolder;
import net.dankito.utils.ThreadPool;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;


public class RequestReceiverTest {

  protected static final int TEST_PORT = 54321;


  protected RequestReceiver underTest;


  protected SocketHandler socketHandler;

  protected IMessageHandler messageHandler;

  protected IMessageSerializer messageSerializer;

  protected IThreadPool threadPool;

  protected INetworkSettings networkSettings;

  protected RequestReceiver requestReceiver2;


  @Before
  public void setUp() throws Exception {
    socketHandler = new SocketHandler();
    networkSettings = new NetworkSettings();
    messageHandler = new MessageHandler(networkSettings);
    messageSerializer = new JsonMessageSerializer();
    threadPool = new ThreadPool();

    underTest = new RequestReceiver(socketHandler, messageHandler, messageSerializer, threadPool);

    requestReceiver2 = new RequestReceiver(socketHandler, messageHandler, messageSerializer, threadPool);
  }

  @After
  public void tearDown() throws Exception {
    underTest.close();

    requestReceiver2.close();
  }


  @Test
  public void start_DesiredPortOutOfExpectedRange() throws Exception {
    final ObjectHolder<Integer> selectedPortHolder = new ObjectHolder<>();
    final CountDownLatch countDownLatch = new CountDownLatch(1);

    networkSettings.addListener(new NetworkSettingsChangedListener() {
      @Override
      public void settingsChanged(INetworkSettings networkSettings, NetworkSetting setting, Object newValue, Object oldValue) {
        selectedPortHolder.setObject((Integer)newValue); // should never come to this
        countDownLatch.countDown();
      }
    });


    int portOutOfRange = (int)Math.pow(2, 16);
    underTest.start(portOutOfRange, networkSettings);


    try { countDownLatch.await(1, TimeUnit.SECONDS); } catch(Exception ignored) { }

    assertThat(selectedPortHolder.isObjectSet(), is(false));
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

    int alreadyBoundPort = TEST_PORT;
    requestReceiver2.start(alreadyBoundPort, networkSettings);

    try { countDownLatchRequestReceiver2.await(1, TimeUnit.SECONDS); } catch(Exception ignored) { }


    final ObjectHolder<Integer> selectedPortHolder = new ObjectHolder<>();
    final CountDownLatch countDownLatchRequestReceiverUnderTest = new CountDownLatch(1);

    networkSettings.addListener(new NetworkSettingsChangedListener() {
      @Override
      public void settingsChanged(INetworkSettings networkSettings, NetworkSetting setting, Object newValue, Object oldValue) {
        if(setting == NetworkSetting.MESSAGES_PORT) {
          selectedPortHolder.setObject((Integer)newValue);
          countDownLatchRequestReceiverUnderTest.countDown();
        }
      }
    });

    underTest.start(alreadyBoundPort, networkSettings);


    try { countDownLatchRequestReceiverUnderTest.await(1, TimeUnit.SECONDS); } catch(Exception ignored) { }

    assertThat(selectedPortHolder.isObjectSet(), is(true));
    assertThat((int)selectedPortHolder.getObject(), is(alreadyBoundPort + 1));
  }


  @Test
  public void close_SocketGetClosed() throws Exception {
    int testPort = TEST_PORT;

    final CountDownLatch countDownLatchReceiver1 = new CountDownLatch(1);

    networkSettings.addListener(new NetworkSettingsChangedListener() {
      @Override
      public void settingsChanged(INetworkSettings networkSettings, NetworkSetting setting, Object newValue, Object oldValue) {
        if(setting == NetworkSetting.MESSAGES_PORT) {
          countDownLatchReceiver1.countDown();
        }
      }
    });

    underTest.start(testPort, networkSettings);

    try { countDownLatchReceiver1.await(1, TimeUnit.SECONDS); } catch(Exception ignored) { }


    underTest.close();


    testIfSocketGotClosed(testPort);
  }

  protected void testIfSocketGotClosed(int testPort) {
    // now start second receiver on some port to check if port is available again
    final CountDownLatch countDownLatchReceiver2 = new CountDownLatch(1);
    final ObjectHolder<Integer> receiver2SelectedPortHolder = new ObjectHolder<>();

    networkSettings.addListener(new NetworkSettingsChangedListener() {
      @Override
      public void settingsChanged(INetworkSettings networkSettings, NetworkSetting setting, Object newValue, Object oldValue) {
        if(setting == NetworkSetting.MESSAGES_PORT) {
          receiver2SelectedPortHolder.setObject((Integer)newValue);
          countDownLatchReceiver2.countDown();
        }
      }
    });

    requestReceiver2.start(testPort, networkSettings);

    try { countDownLatchReceiver2.await(1, TimeUnit.SECONDS); } catch(Exception ignored) { }

    assertThat(receiver2SelectedPortHolder.isObjectSet(), is(true));
    assertThat((int)receiver2SelectedPortHolder.getObject(), is(testPort));
  }

}