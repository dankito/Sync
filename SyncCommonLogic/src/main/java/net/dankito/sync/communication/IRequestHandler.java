package net.dankito.sync.communication;

import net.dankito.communication.message.Request;
import net.dankito.communication.message.Response;


public interface IRequestHandler {

  Response handle(Request request);

}
