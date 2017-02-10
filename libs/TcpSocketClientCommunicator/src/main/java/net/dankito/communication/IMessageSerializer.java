package net.dankito.communication;


import net.dankito.communication.message.Request;
import net.dankito.communication.message.Response;

public interface IMessageSerializer {

  byte[] serializeRequest(Request request) throws Exception;

  Request deserializeRequest(String requestString) throws Exception;

  byte[] serializeResponse(Response response) throws Exception;

  Response deserializeResponse(String methodName, String responseString);

}
