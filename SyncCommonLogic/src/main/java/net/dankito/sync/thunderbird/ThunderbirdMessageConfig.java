package net.dankito.sync.thunderbird;

import java.nio.charset.Charset;


public class ThunderbirdMessageConfig {

  public static final Charset MESSAGES_CHARSET = Charset.forName("UTF-8");

  public static final String GET_ADDRESS_BOOK_MESSAGE = "GetAddressBook";

  public static final String SYNC_CONTACT_MESSAGE = "SyncContact";
}
