package net.dankito.sync.thunderbird;

import net.dankito.communication.IMessageHandler;
import net.dankito.communication.callback.RequestHandlerCallback;
import net.dankito.communication.message.Request;
import net.dankito.sync.thunderbird.model.ThunderbirdContact;


public class ThunderbirdMessageHandler implements IMessageHandler {

  @Override
  public void handleReceivedRequest(Request request, RequestHandlerCallback callback) {

  }

  @Override
  public Class getRequestBodyClassForMethod(String methodName) throws Exception {
    return null;
  }

  @Override
  public Class getResponseBodyClassForMethod(String methodName) throws Exception {
    if(ThunderbirdMessageConfig.GET_ADDRESS_BOOK_MESSAGE.equals(methodName)) {
      return ThunderbirdContact[].class;
    }

    return null;
  }

}
