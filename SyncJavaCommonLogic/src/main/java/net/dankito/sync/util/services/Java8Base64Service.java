package net.dankito.sync.util.services;

import net.dankito.utils.services.IBase64Service;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import javax.inject.Named;
import javax.xml.bind.DatatypeConverter;


@Named
public class Java8Base64Service implements IBase64Service {

  protected static final Charset DEFAULT_CHAR_SET = Charset.forName("UTF-8");


  @Override
  public String encode(String stringToEncode) {
    return encode(stringToEncode.getBytes(DEFAULT_CHAR_SET));
  }

  @Override
  public String encode(byte[] dataToEncode) {
    return Base64.getEncoder().encodeToString(dataToEncode);
  }

  @Override
  public String decode(String stringToDecode) {
    return new String(decodeToBytes(stringToDecode), DEFAULT_CHAR_SET);
  }

  @Override
  public byte[] decodeToBytes(String stringToDecode) {
    return Base64.getDecoder().decode(stringToDecode.getBytes(DEFAULT_CHAR_SET));
  }

}
