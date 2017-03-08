package net.dankito.sync.thunderbird;

import net.dankito.communication.IMessageHandler;
import net.dankito.communication.callback.RequestHandlerCallback;
import net.dankito.communication.message.Request;
import net.dankito.sync.thunderbird.model.ContactSync;
import net.dankito.sync.thunderbird.model.ThunderbirdContact;


public class ThunderbirdMessageHandler implements IMessageHandler {

  @Override
  public void handleReceivedRequest(Request request, RequestHandlerCallback callback) {
    switch(request.getMethod()) {
      case ThunderbirdMessageConfig.SYNC_CONTACT_MESSAGE:
        handleSynchronizedContact(request);
        break;
    }
  }

  protected void handleSynchronizedContact(Request request) {

  }

  @Override
  public Class getRequestBodyClassForMethod(String methodName) throws Exception {
    switch(methodName) {
      case ThunderbirdMessageConfig.SYNC_CONTACT_MESSAGE:
        return ContactSync.class;
    }

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
