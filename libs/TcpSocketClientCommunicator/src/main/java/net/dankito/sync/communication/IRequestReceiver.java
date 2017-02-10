package net.dankito.sync.communication;

public interface IRequestReceiver {

  void start(int desiredMessagesReceiverPort, RequestReceiverCallback callback);

  void close();

}
