package net.dankito.communication.callback;

import net.dankito.communication.message.Response;


public interface SendRequestCallback<T> {

  void done(Response<T> response);

}
