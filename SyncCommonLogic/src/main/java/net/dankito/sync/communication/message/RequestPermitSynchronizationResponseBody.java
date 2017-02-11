package net.dankito.sync.communication.message;


public class RequestPermitSynchronizationResponseBody {

  protected RequestPermitSynchronizationResult result;

  protected String nonce;


  protected RequestPermitSynchronizationResponseBody() { // for Jackson

  }

  public RequestPermitSynchronizationResponseBody(RequestPermitSynchronizationResult result) {
    this.result = result;
  }

  public RequestPermitSynchronizationResponseBody(RequestPermitSynchronizationResult result, String nonce) {
    this(result);
    this.nonce = nonce;
  }


  public RequestPermitSynchronizationResult getResult() {
    return result;
  }

  public String getNonce() {
    return nonce;
  }

}
