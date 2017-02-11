package net.dankito.sync.communication.message;


public class RespondToSynchronizationPermittingChallengeRequestBody {

  protected String nonce;

  protected String challengeResponse;

  protected int synchronizationPort;


  protected RespondToSynchronizationPermittingChallengeRequestBody() { // for Jackson

  }

  public RespondToSynchronizationPermittingChallengeRequestBody(String nonce, String challengeResponse, int synchronizationPort) {
    this.nonce = nonce;
    this.challengeResponse = challengeResponse;
    this.synchronizationPort = synchronizationPort;
  }


  public String getNonce() {
    return nonce;
  }

  public String getChallengeResponse() {
    return challengeResponse;
  }

  public int getSynchronizationPort() {
    return synchronizationPort;
  }

}
