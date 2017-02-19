package net.dankito.sync.communication.message;

import net.dankito.utils.services.HashAlgorithm;
import net.dankito.utils.services.HashService;
import net.dankito.utils.services.IBase64Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import javax.inject.Named;


@Named
public class ChallengeHandler {

  protected static final int DEFAULT_COUNT_RETRIES = 2;

  protected static final HashAlgorithm CHALLENGE_RESPONSE_HASH_ALGORITHM = HashAlgorithm.SHA512;


  protected IBase64Service base64Service;

  protected HashService hashService = new HashService();

  protected Random random = new Random(System.nanoTime());

  protected Map<String, DeviceInfo> nonceToDeviceInfoMap = new HashMap<>();

  protected Map<String, String> nonceToCorrectResponsesMap = new HashMap<>();

  protected Map<String, Integer> nonceToCountRetriesMap = new HashMap<>();


  public ChallengeHandler(IBase64Service base64Service) {
    this.base64Service = base64Service;
  }


  public NonceToResponsePair createChallengeForDevice(DeviceInfo deviceInfo) {
    String nonce = UUID.randomUUID().toString();
    String correctResponse = createCorrectResponse();

    nonceToDeviceInfoMap.put(nonce, deviceInfo);

    nonceToCorrectResponsesMap.put(nonce, correctResponse);

    nonceToCountRetriesMap.put(nonce, DEFAULT_COUNT_RETRIES);

    return new NonceToResponsePair(nonce, correctResponse);
  }

  protected String createCorrectResponse() {
    int response = random.nextInt(1000000);

    return String.format("%06d", response);
  }


  public String createChallengeResponse(String nonce, String enteredCode) {
    String challengeResponse = nonce + "-" + enteredCode;

    try {
      byte[] hashedChallengeResponse = hashService.hashStringToBytes(CHALLENGE_RESPONSE_HASH_ALGORITHM, challengeResponse);

      return base64Service.encode(hashedChallengeResponse);
    } catch(Exception e) { /* should actually never occur */ }

    return null;
  }


  public boolean isResponseOk(String nonce, String base64EncodeChallengeResponse) {
    boolean isCorrectResponse = isCorrectResponse(nonce, base64EncodeChallengeResponse);

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

  protected boolean isCorrectResponse(String nonce, String base64EncodeChallengeResponse) {
    // check if nonceToCorrectResponsesMap really contains nonce as otherwise (null, null) would be a correct response
    if(nonceToCorrectResponsesMap.containsKey(nonce)) {
      String correctResponse = nonceToCorrectResponsesMap.get(nonce);
      String correctChallengeResponse = createChallengeResponse(nonce, correctResponse);

      return base64EncodeChallengeResponse.equals(correctChallengeResponse);
    }

    return false;
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
