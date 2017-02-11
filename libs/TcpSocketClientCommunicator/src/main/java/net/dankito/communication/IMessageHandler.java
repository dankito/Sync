package net.dankito.communication;

import net.dankito.communication.callback.RequestHandlerCallback;
import net.dankito.communication.message.Request;


public interface IMessageHandler {

  void handleReceivedRequest(Request request, RequestHandlerCallback callback);

  Class getRequestBodyClassForMethod(String methodName) throws Exception;

  Class getResponseBodyClassForMethod(String methodName) throws Exception;

}
