package net.dankito.sync.communication;

import net.dankito.sync.devices.DevicesManagerConfig;


public class CommunicatorConfig {

  public static final int DEFAULT_MESSAGES_RECEIVER_PORT = DevicesManagerConfig.DEVICES_DISCOVERER_PORT + 1;


  public static final String GET_DEVICE_INFO_METHOD_NAME = "GetDeviceInfo";

  public static final String REQUEST_PERMIT_SYNCHRONIZATION_METHOD_NAME = "RequestPermitSynchronization";

  public static final String RESPONSE_TO_SYNCHRONIZATION_PERMITTING_CHALLENGE_METHOD_NAME = "ResponseToSynchronizationPermittingChallenge";

  public static final String REQUEST_START_SYNCHRONIZATION_METHOD_NAME = "RequestStartSynchronization";

}
