package net.dankito.sync.communication.message;


public class RequestStartSynchronizationRequestBody {

  protected String uniqueDeviceId;

  protected int synchronizationPort;


  protected RequestStartSynchronizationRequestBody() { // for Jackson

  }

  public RequestStartSynchronizationRequestBody(String uniqueDeviceId, int synchronizationPort) {
    this.uniqueDeviceId = uniqueDeviceId;
    this.synchronizationPort = synchronizationPort;
  }


  public String getUniqueDeviceId() {
    return uniqueDeviceId;
  }

  public int getSynchronizationPort() {
    return synchronizationPort;
  }

}
