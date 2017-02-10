package net.dankito.communication;


public interface RequestReceiverCallback {

  void started(IRequestReceiver requestReceiver, boolean couldStartReceiver, int messagesReceiverPort, Exception startException);

}
