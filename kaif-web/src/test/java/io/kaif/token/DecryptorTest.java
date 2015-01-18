package io.kaif.token;

import static org.junit.Assert.*;

import java.security.SecureRandom;
import java.util.Arrays;

import org.junit.Test;

public class DecryptorTest {
  @Test
  public void decrypt() throws Exception {
    final byte[] bytes = new byte[100];
    final SecureRandom secureRandom = new SecureRandom();
    for (int i = 0; i < 100; i++) {
      final byte[] keys = new byte[16];
      secureRandom.nextBytes(keys);
      secureRandom.nextBytes(bytes);
      final byte[] actual = Decryptor.create(keys).decrypt(Encryptor.create(keys).encrypt(bytes));
      assertTrue(Arrays.equals(actual, bytes));
    }
  }

  @Test
  public void ivPerformance() throws Exception {
    final byte[] bytes = new byte[1000];
    final SecureRandom secureRandom = new SecureRandom();

    final byte[] keys = new byte[16];
    final byte[] iv = new byte[16];

    secureRandom.nextBytes(keys);
    secureRandom.nextBytes(iv);

    final Decryptor decryptor = Decryptor.create(keys, iv);
    final Encryptor encryptor = Encryptor.create(keys, iv);
    for (int i = 0; i < 1000; i++) {
      secureRandom.nextBytes(bytes);

      final byte[] actual = decryptor.decrypt(encryptor.encrypt(bytes));
      assertTrue(Arrays.equals(actual, bytes));

      secureRandom.nextBytes(iv);
      encryptor.resetIvParameter(iv, 0);
      decryptor.resetIvParameter(iv, 0);
    }
  }

}