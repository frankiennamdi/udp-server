package com.franklin.sample.udp.server;

import com.franklin.sample.udp.message.MessageFragment;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.util.List;

@Component
public class ProtocolHandler {

  public MessageFragment processMessageFragment(byte[] pbuf) {
    int length = unsignedShortToInt(getBytes(2, 2, pbuf));
    return new MessageFragment(unsignedShortToInt(getBytes(0, 2, pbuf)),
            unsignedIntToLong(getBytes(4, 4, pbuf)), unsignedIntToLong(getBytes(8, 4, pbuf)),
            getBytes(12, length, pbuf));
  }

  public byte[] concatenateByteArrays(List<byte[]> blocks) {
    ByteArrayOutputStream os = new ByteArrayOutputStream();
    for (byte[] b : blocks) {
      os.write(b, 0, b.length);
    }
    return os.toByteArray();
  }

  private byte[] getBytes(int position, int len, byte[] pbuf) {
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
