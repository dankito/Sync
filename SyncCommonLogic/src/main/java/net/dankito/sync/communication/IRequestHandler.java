package net.dankito.sync.communication;

import net.dankito.communication.message.Request;
import net.dankito.sync.communication.callback.RequestHandlerCallback;


public interface IRequestHandler {

  void handle(Request request, RequestHandlerCallback callback);

}
