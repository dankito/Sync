package net.dankito.sync.communication.message;


import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ChallengeHandler {

  protected static final int DEFAULT_COUNT_RETRIES = 2;


  protected Map<String, DeviceInfo> nonceToDeviceInfoMap = new HashMap<>();

  protected Map<String, String> nonceToCorrectResponsesMap = new HashMap<>();

  protected Map<String, Integer> nonceToCountRetriesMap = new HashMap<>();


  public NonceToResponsePair createChallengeForDevice(DeviceInfo deviceInfo) {
    String nonce = UUID.randomUUID().toString();
    String correctResponse = createCorrectResponse();

    nonceToDeviceInfoMap.put(nonce, deviceInfo);

    nonceToCorrectResponsesMap.put(nonce, correctResponse);

    nonceToCountRetriesMap.put(nonce, DEFAULT_COUNT_RETRIES);

    return new NonceToResponsePair(nonce, correctResponse);
  }

  protected String createCorrectResponse() {
    return "1111"; // TODO
  }


  public boolean isResponseOk(String nonce, String challengeResponse) {
    // check if nonceToCorrectResponsesMap really contains nonce as otherwise (null, null) would be a correct response
    boolean isCorrectResponse = nonceToCorrectResponsesMap.containsKey(nonce) && challengeResponse.equals(nonceToCorrectResponsesMap.get(nonce));

    if(isCorrectResponse) {
      nonceToCountRetriesMap.remove(nonce);
      nonceToCorrectResponsesMap.remove(nonce);
    }
    else {
      int countRetries = nonceToCountRetriesMap.get(nonce) - 1;

      if(countRetries > 0) {
        nonceToCountRetriesMap.put(nonce, countRetries);
      }
      else {
        nonceToCountRetriesMap.remove(nonce);
      }
    }

    return isCorrectResponse;
  }

  public int getCountRetriesLeftForNonce(String nonce) {
    if(nonceToCountRetriesMap.containsKey(nonce)) {
      return nonceToCountRetriesMap.get(nonce);
    }

    return 0;
  }

  public DeviceInfo getDeviceInfoForNonce(String nonce) {
    return nonceToDeviceInfoMap.get(nonce);
  }

}
