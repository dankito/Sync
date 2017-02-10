package net.dankito.communication;

import com.couchbase.lite.support.Base64;

import net.dankito.communication.callback.SendRequestCallback;
import net.dankito.communication.util.TestResponseBody;
import net.dankito.sync.communication.message.DeviceInfo;
import net.dankito.communication.message.Request;
import net.dankito.communication.message.Response;
import net.dankito.communication.message.ResponseErrorType;
import net.dankito.utils.IThreadPool;
import net.dankito.utils.ObjectHolder;
import net.dankito.utils.ThreadPool;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.matchers.JUnitMatchers.containsString;


public class RequestSenderTest {

  protected static final int MESSAGES_RECEIVER_PORT = 54321;

  protected static final String TEST_METHOD_NAME = "RequestSenderTest";

  protected static final String TEST_STRING = "Love";

  protected static final float TEST_FLOAT = 3.14f;


  protected IRequestSender underTest;


  protected SocketAddress destinationAddress;

  protected RequestReceiver remoteRequestReceiver;

  protected IMessageHandler remoteMessageHandler;

  protected IThreadPool threadPool;

  protected SocketHandler socketHandler;

  protected IMessageSerializer messageSerializer;

  protected Request testRequest = new Request(TEST_METHOD_NAME);

  protected TestResponseBody testResponseBody = new TestResponseBody(TEST_STRING, TEST_FLOAT);

  protected Response<TestResponseBody> testResponse = new Response(testResponseBody);


  @Before
  public void setUp() throws Exception {
    socketHandler = Mockito.spy(new SocketHandler());
    threadPool = new ThreadPool();

    setupRemoteMessagesReceiver();

    underTest = new RequestSender(socketHandler, messageSerializer, threadPool);
  }

  protected void setupRemoteMessagesReceiver() throws Exception {
    final CountDownLatch waitForMessagesReceiverBeingSetupLatch = new CountDownLatch(1);

    remoteMessageHandler = Mockito.mock(IMessageHandler.class);
    Mockito.doReturn(null).when(remoteMessageHandler).getRequestBodyClassForMethod(TEST_METHOD_NAME);
    Mockito.doReturn(TestResponseBody.class).when(remoteMessageHandler).getResponseBodyClassForMethod(TEST_METHOD_NAME);
    Mockito.doAnswer(new Answer<Response>() {
      @Override
      public Response answer(InvocationOnMock invocation) throws Throwable {
        Request request = (Request)invocation.getArguments()[0];
        if(TEST_METHOD_NAME.equals(request.getMethod())) {
          return testResponse;
        }
        return null;
      }
    }).when(remoteMessageHandler).handleReceivedRequest(Mockito.any(Request.class));

    messageSerializer = Mockito.spy(new JsonMessageSerializer(remoteMessageHandler));

    remoteRequestReceiver = Mockito.spy(new RequestReceiver(socketHandler, remoteMessageHandler, messageSerializer, threadPool));
    remoteRequestReceiver.start(MESSAGES_RECEIVER_PORT, new RequestReceiverCallback() {
      @Override
      public void started(IRequestReceiver requestReceiver, boolean couldStartReceiver, int messagesReceiverPort, Exception startException) {
        if(couldStartReceiver) {
          destinationAddress = new InetSocketAddress("localhost", messagesReceiverPort);
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
  public void sendRequestAndReceiveResponseAsync() throws Exception {
    final ObjectHolder<Response<TestResponseBody>> responseHolder = new ObjectHolder<>();
    final CountDownLatch countDownLatch = new CountDownLatch(1);

    underTest.sendRequestAndReceiveResponseAsync(destinationAddress, testRequest, new SendRequestCallback<TestResponseBody>() {
      @Override
      public void done(Response<TestResponseBody> response) {
        responseHolder.setObject(response);
        countDownLatch.countDown();
      }
    });

    try { countDownLatch.await(1, TimeUnit.SECONDS); } catch(Exception ignored) { }

    assertThat(responseHolder.isObjectSet(), is(true));

    Response<TestResponseBody> response = responseHolder.getObject();
    assertThat(response.isCouldHandleMessage(), is(true));

    TestResponseBody responseBody = response.getBody();
    assertThat(responseBody.getString(), is(TEST_STRING));
    assertThat(responseBody.getaFloat(), is(TEST_FLOAT));
  }


  @Test
  public void sendRequestToClosedClient() throws Exception {
    remoteRequestReceiver.close();

    final ObjectHolder<Response<DeviceInfo>> responseHolder = new ObjectHolder<>();
    final CountDownLatch countDownLatch = new CountDownLatch(1);

    underTest.sendRequestAndReceiveResponseAsync(destinationAddress, testRequest, new SendRequestCallback<DeviceInfo>() {
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

    underTest.sendRequestAndReceiveResponseAsync(destinationAddress, testRequest, new SendRequestCallback<DeviceInfo>() {
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

    underTest.sendRequestAndReceiveResponseAsync(destinationAddress, testRequest, new SendRequestCallback<DeviceInfo>() {
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

    underTest.sendRequestAndReceiveResponseAsync(destinationAddress, testRequest, new SendRequestCallback<DeviceInfo>() {
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

    underTest.sendRequestAndReceiveResponseAsync(destinationAddress, testRequest, new SendRequestCallback<DeviceInfo>() {
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

    underTest.sendRequestAndReceiveResponseAsync(destinationAddress, testRequest, new SendRequestCallback<DeviceInfo>() {
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

    underTest.sendRequestAndReceiveResponseAsync(destinationAddress, testRequest, new SendRequestCallback<DeviceInfo>() {
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

    testResponseBody.setString(stringThatExceedsMaxMessageSize.toString());

    final ObjectHolder<Response<DeviceInfo>> responseHolder = new ObjectHolder<>();
    final CountDownLatch countDownLatch = new CountDownLatch(1);

    underTest.sendRequestAndReceiveResponseAsync(destinationAddress, testRequest, new SendRequestCallback<DeviceInfo>() {
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

    underTest.sendRequestAndReceiveResponseAsync(destinationAddress, testRequest, new SendRequestCallback<DeviceInfo>() {
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

    underTest.sendRequestAndReceiveResponseAsync(destinationAddress, testRequest, new SendRequestCallback<DeviceInfo>() {
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