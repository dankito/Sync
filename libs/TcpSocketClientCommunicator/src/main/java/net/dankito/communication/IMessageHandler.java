package net.dankito.communication;

import net.dankito.communication.message.Request;
import net.dankito.communication.message.Response;


public interface IMessageHandler {

  Response handleReceivedRequest(Request request);

  Class getRequestBodyClassForMethod(String methodName) throws Exception;

  Class getResponseBodyClassForMethod(String methodName) throws Exception;

}
