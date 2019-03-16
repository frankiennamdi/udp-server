package com.franklin.sample.udp.server;

import javax.xml.bind.DatatypeConverter;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Sha256Helper {

  private static final MessageDigest SHA256;

  static {
    try {
      SHA256 = MessageDigest.getInstance("SHA-256");
    } catch (NoSuchAlgorithmException e) {
      throw new RuntimeException(e);
    }
  }

  public static String bytesToSha256(byte[] bytes){
    return DatatypeConverter.printHexBinary(SHA256.digest(bytes)).toLowerCase();
  }
}
