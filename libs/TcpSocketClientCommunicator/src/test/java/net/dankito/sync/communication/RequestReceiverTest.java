package net.dankito.sync.communication;

import net.dankito.utils.IThreadPool;
import net.dankito.utils.ObjectHolder;
import net.dankito.utils.ThreadPool;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;


public class RequestReceiverTest {

  protected static final int TEST_PORT = 54321;


  protected RequestReceiver underTest;


  protected RequestReceiver requestReceiver2;


  @Before
  public void setUp() throws Exception {
    SocketHandler socketHandler = Mockito.mock(SocketHandler.class);
    IMessageHandler messageHandler = Mockito.mock(IMessageHandler.class);
    IMessageSerializer messageSerializer = Mockito.mock(IMessageSerializer.class);
    IThreadPool threadPool = new ThreadPool();

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
    final ObjectHolder<Boolean> startResultHolder = new ObjectHolder<>();
    final CountDownLatch countDownLatch = new CountDownLatch(1);


    int portOutOfRange = (int)Math.pow(2, 16);
    underTest.start(portOutOfRange, new RequestReceiverCallback() {
      @Override
      public void started(IRequestReceiver requestReceiver, boolean couldStartReceiver, int messagesReceiverPort, Exception startException) {
        startResultHolder.setObject(couldStartReceiver);
        countDownLatch.countDown();
      }
    });


    try { countDownLatch.await(1, TimeUnit.SECONDS); } catch(Exception ignored) { }

    assertThat(startResultHolder.isObjectSet(), is(true));
    assertThat(startResultHolder.getObject(), is(false));
  }

  @Test
  public void start_DesiredPortAlreadyBound() throws Exception {
    final CountDownLatch countDownLatchRequestReceiver2 = new CountDownLatch(1);

    int alreadyBoundPort = TEST_PORT;
    requestReceiver2.start(alreadyBoundPort, new RequestReceiverCallback() {
      @Override
      public void started(IRequestReceiver requestReceiver, boolean couldStartReceiver, int messagesReceiverPort, Exception startException) {
        countDownLatchRequestReceiver2.countDown();
      }
    });

    try { countDownLatchRequestReceiver2.await(1, TimeUnit.SECONDS); } catch(Exception ignored) { }


    final ObjectHolder<Integer> selectedPortHolder = new ObjectHolder<>();
    final CountDownLatch countDownLatchRequestReceiverUnderTest = new CountDownLatch(1);


    underTest.start(alreadyBoundPort, new RequestReceiverCallback() {
      @Override
      public void started(IRequestReceiver requestReceiver, boolean couldStartReceiver, int messagesReceiverPort, Exception startException) {
        if(couldStartReceiver) {
          selectedPortHolder.setObject(messagesReceiverPort);
          countDownLatchRequestReceiverUnderTest.countDown();
        }
      }
    });


    try { countDownLatchRequestReceiverUnderTest.await(1, TimeUnit.SECONDS); } catch(Exception ignored) { }

    assertThat(selectedPortHolder.isObjectSet(), is(true));
    assertThat((int)selectedPortHolder.getObject(), is(alreadyBoundPort + 1));
  }


  @Test
  public void close_SocketGetClosed() throws Exception {
    int testPort = TEST_PORT;

    final CountDownLatch countDownLatchReceiver1 = new CountDownLatch(1);

    underTest.start(testPort, new RequestReceiverCallback() {
      @Override
      public void started(IRequestReceiver requestReceiver, boolean couldStartReceiver, int messagesReceiverPort, Exception startException) {
        if(couldStartReceiver) {
          countDownLatchReceiver1.countDown();
        }
      }
    });

    try { countDownLatchReceiver1.await(1, TimeUnit.SECONDS); } catch(Exception ignored) { }


    underTest.close();


    testIfSocketGotClosed(testPort);
  }

  protected void testIfSocketGotClosed(int testPort) {
    // now start second receiver on some port to check if port is available again
    final CountDownLatch countDownLatchReceiver2 = new CountDownLatch(1);
    final ObjectHolder<Integer> receiver2SelectedPortHolder = new ObjectHolder<>();

    requestReceiver2.start(testPort, new RequestReceiverCallback() {
      @Override
      public void started(IRequestReceiver requestReceiver, boolean couldStartReceiver, int messagesReceiverPort, Exception startException) {
        if(couldStartReceiver) {
          receiver2SelectedPortHolder.setObject(messagesReceiverPort);
          countDownLatchReceiver2.countDown();
        }
      }
    });

    try { countDownLatchReceiver2.await(1, TimeUnit.SECONDS); } catch(Exception ignored) { }

    assertThat(receiver2SelectedPortHolder.isObjectSet(), is(true));
    assertThat((int)receiver2SelectedPortHolder.getObject(), is(testPort));
  }

}