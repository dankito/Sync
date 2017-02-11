package net.dankito.sync.communication.message;


public class RequestStartSynchronizationRequestBody {

  protected String uniqueDeviceId;


  protected RequestStartSynchronizationRequestBody() { // for Jackson

  }

  public RequestStartSynchronizationRequestBody(String uniqueDeviceId) {
    this.uniqueDeviceId = uniqueDeviceId;
  }


  public String getUniqueDeviceId() {
    return uniqueDeviceId;
  }

}
