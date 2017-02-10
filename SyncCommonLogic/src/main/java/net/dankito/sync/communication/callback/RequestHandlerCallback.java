package net.dankito.sync.communication.callback;

import net.dankito.communication.message.Response;


public interface RequestHandlerCallback {

  void done(Response response);

}
