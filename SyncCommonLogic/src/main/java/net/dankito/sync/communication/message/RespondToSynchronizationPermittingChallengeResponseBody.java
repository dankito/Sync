package net.dankito.sync.communication.message;


public class RespondToSynchronizationPermittingChallengeResponseBody {

  protected RespondToSynchronizationPermittingChallengeResult result;

  protected int countRetriesLeft = 0;

  protected int synchronizationPort = 0;


  protected RespondToSynchronizationPermittingChallengeResponseBody() { // for Jackson

  }

  public RespondToSynchronizationPermittingChallengeResponseBody(RespondToSynchronizationPermittingChallengeResult result) {
    this.result = result;
  }

  public RespondToSynchronizationPermittingChallengeResponseBody(RespondToSynchronizationPermittingChallengeResult result, int countRetriesLeft) {
    this(result);
    this.countRetriesLeft = countRetriesLeft;
  }

  public RespondToSynchronizationPermittingChallengeResponseBody(int synchronizationPort) {
    this(RespondToSynchronizationPermittingChallengeResult.ALLOWED);

    this.synchronizationPort = synchronizationPort;
  }


  public RespondToSynchronizationPermittingChallengeResult getResult() {
    return result;
  }

  public int getCountRetriesLeft() {
    return countRetriesLeft;
  }

  public int getSynchronizationPort() {
    return synchronizationPort;
  }

}
