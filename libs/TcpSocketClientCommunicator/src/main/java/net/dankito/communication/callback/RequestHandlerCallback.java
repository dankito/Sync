package net.dankito.communication.callback;

import net.dankito.communication.message.Response;


public interface RequestHandlerCallback {

  void done(Response response);

}
