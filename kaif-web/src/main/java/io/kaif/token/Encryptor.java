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
import java.util.Base64.Encoder;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.ShortBufferException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * iOS use AES/CBC/PKCS7Padding instead of PKCS#5, for key length <= 256bit, PKCS#7 and PKCS#5 are
 * identical
 */
class Encryptor {
  private static final int KEY_LENGTH = 16;
  final static Encoder URL_SAFE_BASE64_ENCODER = Base64.getUrlEncoder().withoutPadding();

  /**
   * key are 16 bytes, with 16 bytes-zero iv
   */
  public static Encryptor create(final byte[] key) {
    final byte[] iv = new byte[KEY_LENGTH];
    Arrays.fill(iv, (byte) 0x00);
    return create(key, iv);
  }

  /**
   * both key and iv are 16 bytes
   *
   * @param key
   * @param iv
   *     16 bytes initial vector
   */
  public static Encryptor create(final byte[] key, final byte[] iv) {
    try {
      final IvParameterSpec ivParameterSpec = new IvParameterSpec(iv);

      return new Encryptor(key, "AES/CBC/PKCS5Padding", ivParameterSpec);
    } catch (final GeneralSecurityException e) {
      e.printStackTrace();
      throw new RuntimeException("unsupported type of encryption: AES");
    }
  }

  private final Cipher encryptCipher;
  private final Key key;

  private Encryptor(final byte[] rawKeyData,
      final String algorithm,
      final IvParameterSpec ivParameterSpec)
      throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException,
      InvalidAlgorithmParameterException {
    key = new SecretKeySpec(rawKeyData, "AES");
    encryptCipher = Cipher.getInstance(algorithm);
    encryptCipher.init(Cipher.ENCRYPT_MODE, key, ivParameterSpec);
  }

  public byte[] encrypt(final byte[] rawData) throws GeneralSecurityException {
    if (rawData == null) {
      return null;
    }
    return encryptCipher.doFinal(rawData);
  }

  public String encrypt(final String rawData) throws GeneralSecurityException {
    if (rawData == null) {
      return null;
    }
    try {
      return URL_SAFE_BASE64_ENCODER.encodeToString(encrypt(rawData.getBytes("UTF-8")));
    } catch (final UnsupportedEncodingException e) {
      throw new RuntimeException(e);
    }
  }

  public void encryptInto(final byte[] rawData, final int inputLength, final ByteBuffer out)
      throws ShortBufferException, GeneralSecurityException {
    if (rawData == null) {
      out.clear();
      return;
    }
    final int outSize = encryptCipher.doFinal(rawData, 0, inputLength, out.array());
    out.limit(outSize);
  }

  public int estimateByteSize(final int inputByteSize) {
    return encryptCipher.getOutputSize(inputByteSize);
  }

  /**
   * reset cipher to initialized state, with updated IV
   *
   * @param iv
   *     16 bytes initial vector
   */
  public void resetIvParameter(final byte[] iv, final int offset) {
    try {
      encryptCipher.init(Cipher.ENCRYPT_MODE, key, new IvParameterSpec(iv, offset, KEY_LENGTH));
    } catch (InvalidKeyException | InvalidAlgorithmParameterException e) {
      throw new RuntimeException("unexpected", e);
    }
  }
}
