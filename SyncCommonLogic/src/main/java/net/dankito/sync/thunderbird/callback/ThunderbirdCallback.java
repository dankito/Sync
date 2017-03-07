package net.dankito.sync.thunderbird.callback;

import net.dankito.sync.communication.message.Response;


public interface ThunderbirdCallback<T extends Response> {

  void done(T response);

}
