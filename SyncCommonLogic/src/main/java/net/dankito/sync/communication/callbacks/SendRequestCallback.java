package net.dankito.sync.communication.callbacks;

import net.dankito.sync.communication.message.Response;


public interface SendRequestCallback<T> {

  void done(Response<T> response);

}
