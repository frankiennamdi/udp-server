package com.franklin.sample.udp.server;

import org.springframework.stereotype.Component;

import javax.xml.bind.DatatypeConverter;
import java.io.ByteArrayOutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

@Component
public class ProtocolHandler {

  private final MessageDigest sha256;

  public ProtocolHandler() {
    try {
      sha256 = MessageDigest.getInstance("SHA-256");
    } catch (NoSuchAlgorithmException e) {
      throw new RuntimeException(e);
    }
  }

  public MessageFragment processMessage(byte[] pbuf) {
    int length = unsignedShortToInt(printByte(2, 2, pbuf));
    return new MessageFragment(unsignedShortToInt(printByte(0, 2, pbuf)),
            unsignedIntToLong(printByte(4, 4, pbuf)), unsignedIntToLong(printByte(8, 4, pbuf)),
            printByte(12, length, pbuf));
  }

  public String sha256(byte[] convertme){
    return DatatypeConverter.printHexBinary(sha256.digest(convertme)).toLowerCase();
  }

  public byte[] concatenateByteArrays(List<byte[]> blocks) {
    ByteArrayOutputStream os = new ByteArrayOutputStream();
    for (byte[] b : blocks) {
      os.write(b, 0, b.length);
    }
    return os.toByteArray();
  }

  private byte[] printByte(int position, int len, byte[] pbuf) {

    byte[] bytes = new byte[len];
    for (int i = position, pos = 0; pos < len; i++, pos++) {
      bytes[pos] = pbuf[i];
    }
    return bytes;
  }

  private int unsignedShortToInt(byte[] b) {
    int result = 0;
    result |= b[0] & 0xFF;
    result <<= 8;
    result |= b[1] & 0xFF;
    return result;
  }

  private long unsignedIntToLong(byte[] b) {
    long result = 0;
    result |= b[0] & 0xFF;
    result <<= 8;
    result |= b[1] & 0xFF;
    result <<= 8;
    result |= b[2] & 0xFF;
    result <<= 8;
    result |= b[3] & 0xFF;
    return result;
  }
}
