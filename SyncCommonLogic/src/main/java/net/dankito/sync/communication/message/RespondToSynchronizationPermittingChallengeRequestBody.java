package net.dankito.sync.communication.message;


public class RespondToSynchronizationPermittingChallengeRequestBody {

  protected String nonce;

  protected String challengeResponse;


  protected RespondToSynchronizationPermittingChallengeRequestBody() { // for Jackson

  }

  public RespondToSynchronizationPermittingChallengeRequestBody(String nonce, String challengeResponse) {
    this.nonce = nonce;
    this.challengeResponse = challengeResponse;
  }


  public String getNonce() {
    return nonce;
  }

  public String getChallengeResponse() {
    return challengeResponse;
  }

}
