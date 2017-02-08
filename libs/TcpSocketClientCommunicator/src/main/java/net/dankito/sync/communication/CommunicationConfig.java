package net.dankito.sync.communication;


import java.nio.charset.Charset;

public class CommunicationConfig {

  public static final int MAX_MESSAGE_SIZE = 2 * 1024 * 1024;

  public static final int BUFFER_SIZE = 16 * 1024;

  public static final String MESSAGE_CHARSET_NAME = "UTF-8";

  public static final Charset MESSAGE_CHARSET = Charset.forName(MESSAGE_CHARSET_NAME);

  public static final String METHOD_NAME_AND_BODY_SEPARATOR = ":";


  public static final String GET_DEVICE_INFO_METHOD_NAME = "GetDeviceInfo";

}
