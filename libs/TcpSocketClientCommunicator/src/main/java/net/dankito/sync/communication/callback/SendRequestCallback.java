package net.dankito.sync.communication.callback;

import net.dankito.sync.communication.message.Response;


public interface SendRequestCallback<T> {

  void done(Response<T> response);

}
