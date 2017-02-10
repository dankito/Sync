package net.dankito.communication;


public class SocketResult {

  protected boolean isSuccessful;

  protected Exception error;

  protected String receivedMessage;


  public SocketResult(boolean isSuccessful) {
    this.isSuccessful = isSuccessful;
  }

  public SocketResult(Exception error) {
    this(false);
    this.error = error;
  }

  public SocketResult(String receivedMessage) {
    this(true);
    this.receivedMessage = receivedMessage;
  }


  public boolean isSuccessful() {
    return isSuccessful;
  }

  public Exception getError() {
    return error;
  }

  public String getReceivedMessage() {
    return receivedMessage;
  }


  @Override
  public String toString() {
    if(isSuccessful()) {
      if(getReceivedMessage() != null) {
        return "Successful: " + getReceivedMessage();
      }
      else {
        return "Successful";
      }
    }
    else {
      return "Error: " + getError();
    }
  }

}
