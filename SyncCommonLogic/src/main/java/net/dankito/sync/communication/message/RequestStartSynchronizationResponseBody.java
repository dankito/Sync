package net.dankito.sync.communication.message;


public class RequestStartSynchronizationResponseBody {

  protected RequestStartSynchronizationResult result;

  protected int synchronizationPort;


  protected RequestStartSynchronizationResponseBody() { // for Jackson

  }

  public RequestStartSynchronizationResponseBody(RequestStartSynchronizationResult result) {
    this.result = result;
  }

  public RequestStartSynchronizationResponseBody(RequestStartSynchronizationResult result, int synchronizationPort) {
    this.result = result;
    this.synchronizationPort = synchronizationPort;
  }


  public RequestStartSynchronizationResult getResult() {
    return result;
  }

  public int getSynchronizationPort() {
    return synchronizationPort;
  }

}
