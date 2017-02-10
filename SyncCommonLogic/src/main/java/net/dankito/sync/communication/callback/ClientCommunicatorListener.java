package net.dankito.sync.communication.callback;


public interface ClientCommunicatorListener {

  void started(boolean couldStartMessagesReceiver, int messagesReceiverPort, Exception startException);

}
