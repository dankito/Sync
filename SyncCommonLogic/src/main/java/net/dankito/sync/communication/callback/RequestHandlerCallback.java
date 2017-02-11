package net.dankito.sync.communication.callback;

import net.dankito.communication.message.Response;


public interface RequestHandlerCallback {

  void done(Response response); // TODO: it should actually return a net.dankito.sync.communication.message.Response to keep layer consistency, but would needed double mapping

}
