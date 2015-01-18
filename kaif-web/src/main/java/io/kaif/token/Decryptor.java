package io.kaif.token;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.security.GeneralSecurityException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.ShortBufferException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

class Decryptor {
  private static final int KEY_LENGTH = 16;

  /**
   * @see Encryptor#create(byte[]) for key algorithm
   */
  public static Decryptor create(final byte[] key) {
    final byte[] iv = new byte[KEY_LENGTH];
    Arrays.fill(iv, (byte) 0x00);
    return create(key, iv);
  }

  /**
   * @see Encryptor#create(byte[], byte[])) for key algorithm
   */
  public static Decryptor create(final byte[] key, final byte[] iv) {
    try {
      final IvParameterSpec ivParameterSpec = new IvParameterSpec(iv);
      return new Decryptor(key, "AES/CBC/PKCS5Padding", ivParameterSpec);
    } catch (final GeneralSecurityException e) {
      e.printStackTrace();
      throw new RuntimeException("unsupported type of encryption: AES");
    }
  }
  private final Cipher decryptCipher;
  private final Key key;

  private Decryptor(final byte[] rawKeyData,
      final String algorithm,
      final IvParameterSpec ivParameterSpec)
      throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException,
      InvalidAlgorithmParameterException {
    key = new SecretKeySpec(rawKeyData, "AES");

    decryptCipher = Cipher.getInstance(algorithm);
    decryptCipher.init(Cipher.DECRYPT_MODE, key, ivParameterSpec);
  }

  public byte[] decrypt(final byte[] rawData) throws GeneralSecurityException {
    if (rawData == null) {
      return null;
    }
    return decryptCipher.doFinal(rawData);
  }

  public String decrypt(final String rawData) throws GeneralSecurityException {
    if (rawData == null) {
      return null;
    }
    try {
      return new String(decrypt(Base64.getUrlDecoder().decode(rawData)), "UTF-8");
    } catch (final UnsupportedEncodingException e) {
      throw new RuntimeException(e);
    }
  }

  public void decryptInto(final ByteBuffer input, final ByteBuffer output)
      throws ShortBufferException, GeneralSecurityException {
    if (input == null) {
      output.clear();
      return;
    }
    decryptCipher.doFinal(input, output);
  }

  public int estimateByteSize(final int inputByteSize) {
    return decryptCipher.getOutputSize(inputByteSize);
  }

  /**
   * reset cipher to initialized state, with updated IV
   *
   * @param iv
   *     16 bytes initial vector
   */
  public void resetIvParameter(final byte[] iv, final int offset) {
    try {
      decryptCipher.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(iv, offset, KEY_LENGTH));
    } catch (InvalidKeyException | InvalidAlgorithmParameterException e) {
      throw new RuntimeException("unexpected", e);
    }
  }
}
