package net.dankito.sync.communication;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import net.dankito.sync.communication.message.Request;
import net.dankito.sync.communication.message.Response;
import net.dankito.sync.communication.message.ResponseErrorType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;


public class JsonMessageSerializer implements IMessageSerializer {

  private static final Logger log = LoggerFactory.getLogger(JsonMessageSerializer.class);


  protected IMessageHandler messageHandler;

  protected ObjectMapper objectMapper;


  public JsonMessageSerializer(IMessageHandler messageHandler) {
    this.messageHandler = messageHandler;

    this.objectMapper = new ObjectMapper();
    objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
  }


  @Override
  public byte[] serializeRequest(Request request) throws Exception {
    String requestBodyString = null;
    if(request.isBodySet()) {
      requestBodyString = serializeObject(request.getBody());
    }

    String requestString = createRequestString(request.getMethod(), requestBodyString);
    return getBytesFromString(requestString);
  }

  protected String createRequestString(String methodName, String body) {
    return methodName + CommunicationConfig.METHOD_NAME_AND_BODY_SEPARATOR + body;
  }


  @Override
  public Request deserializeRequest(String requestString) throws Exception {
    String[] requestParts = requestString.split(CommunicationConfig.METHOD_NAME_AND_BODY_SEPARATOR, 2);

    String methodName = requestParts[0];
    Object requestBody = null;

    if(requestParts.length > 1) { // requestParts.length == 1 -> request without a body
      String requestBodyString = requestParts[1];
      requestBody = deserializeRequestBody(methodName, requestBodyString);
    }

    return new Request(methodName, requestBody);
  }

  protected Object deserializeRequestBody(String methodName, String requestBodyString) throws Exception {
    Class requestBodyClass = messageHandler.getRequestBodyClassForMethod(methodName);

    if(requestBodyClass != null) {
      return deserializeObject(requestBodyString, requestBodyClass);
    }

    return null;
  }


  @Override
  public byte[] serializeResponse(Response response) throws Exception {
    String serializedResponse = serializeObject(response);
    return getBytesFromString(serializedResponse);
  }


  @Override
  public Response deserializeResponse(String methodName, String responseString) {
    try {
      Class responseBodyType = messageHandler.getResponseBodyClassForMethod(methodName);
      return deserializeObject(responseString, Response.class, responseBodyType);
    } catch(Exception e) {
      log.error("Could not deserialize response " + responseString, e);
      return new Response(ResponseErrorType.DESERIALIZE_RESPONSE, e);
    }
  }


  protected String serializeObject(Object object) throws Exception {
    return objectMapper.writeValueAsString(object);
  }

  protected <T> T deserializeObject(String serializedObject, Class<T> objectClass, Class... genericParameterTypes) throws IOException {
    if(genericParameterTypes.length == 0) {
      return objectMapper.readValue(serializedObject, objectClass);
    }
    else {
      return objectMapper.readValue(serializedObject, objectMapper.getTypeFactory().constructParametricType(objectClass, genericParameterTypes));
    }
  }

  protected byte[] getBytesFromString(String string) {
    return string.getBytes(CommunicationConfig.MESSAGE_CHARSET);
  }

}
