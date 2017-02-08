package net.dankito.sync.communication;

import net.dankito.sync.communication.message.Request;
import net.dankito.sync.communication.message.Response;


public interface IMessageHandler {

  Response handleReceivedRequest(Request request);

}
