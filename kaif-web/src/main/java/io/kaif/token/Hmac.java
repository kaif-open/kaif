package io.kaif.token;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

class Hmac {

  public static Hmac createSHA1(final String key) {
    try {
      return new Hmac(key.getBytes("UTF-8"));
    } catch (final UnsupportedEncodingException e) {
      throw new RuntimeException("unexpected", e);
    }
  }

  private final Mac mac;

  private Hmac(final byte[] keys) {
    try {
      mac = Mac.getInstance("HmacSHA1");
      final SecretKeySpec secret = new SecretKeySpec(keys, mac.getAlgorithm());
      mac.init(secret);
    } catch (final InvalidKeyException e) {
      throw new RuntimeException(e);
    } catch (final NoSuchAlgorithmException e) {
      throw new RuntimeException(e);
    }
  }

  public synchronized byte[] digest(final byte[] data) {
    return mac.doFinal(data);
  }

}
