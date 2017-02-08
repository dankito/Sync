package net.dankito.sync.communication;


import net.dankito.sync.communication.message.Request;
import net.dankito.sync.communication.message.Response;

public interface IMessageSerializer {

  byte[] serializeRequest(Request request) throws Exception;

  Request deserializeRequest(String requestString) throws Exception;

  byte[] serializeResponse(Response response) throws Exception;

  Response deserializeResponse(String methodName, String responseString);

}
