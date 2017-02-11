package net.dankito.sync.communication.message;


public class NonceToResponsePair {

  protected String nonce;

  protected String correctResponse;


  public NonceToResponsePair(String nonce, String correctResponse) {
    this.nonce = nonce;
    this.correctResponse = correctResponse;
  }


  public String getNonce() {
    return nonce;
  }

  public String getCorrectResponse() {
    return correctResponse;
  }

}
