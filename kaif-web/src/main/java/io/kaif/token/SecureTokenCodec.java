package io.kaif.token;

import static java.util.stream.Collectors.*;

import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import com.google.common.base.Preconditions;

public class SecureTokenCodec {
  private static class LazyHolder {
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
  }
  private static final Charset UTF8 = Charset.forName("UTF-8");
  private static final byte[] EMPTY_BYTES = new byte[0];
  private static final int RANDOM_IV_LENGTH = 16;
  private static final byte VERSION_2 = 2;
  private static final int VERSION_LENGTH = 1;
  private static final int MAC_LENGTH = 20;
  private static final int MAX_FIELD_SIZE = 10;

  /**
   * convert String {@link #generateUrlSafeKey()} to bytes
   */
  public static byte[] convertUrlSafeKeyToBytes(String generatedUrlSafeKey) {
    return Base64.getUrlDecoder().decode(generatedUrlSafeKey);
  }

  public static SecureTokenCodec create(final byte[] macKey, final byte[] secretKeyIn16Bytes) {
    return new SecureTokenCodec(macKey, secretKeyIn16Bytes);
  }

  private static byte[] generateMacForIvCipherText(final byte[] macKey, final byte[] ivCipherText)
      throws GeneralSecurityException {
    return hmac(macKey, ivCipherText);
  }

  /**
   * a helper to generate random secured 16 bytes key, encode with url safe base64 (no padding)
   * <p>
   * you should use {@link #convertUrlSafeKeyToBytes(String)} to convert it back to bytes
   */
  public static String generateUrlSafeKey() {
    final byte[] bytes = new byte[16];
    LazyHolder.SECURE_RANDOM.nextBytes(bytes);
    return Encryptor.URL_SAFE_BASE64_ENCODER.encodeToString(bytes);
  }

  private static byte[] hmac(final byte[] key, final byte[] data) throws GeneralSecurityException {
    final Mac mac = Mac.getInstance("HMACSHA1");
    mac.init(new SecretKeySpec(key, ""));
    return mac.doFinal(data);
  }

  public static void main(String[] args) {
    System.out.println("mac:");
    System.out.println(generateUrlSafeKey());
    System.out.println("key:");
    System.out.println(generateUrlSafeKey());
  }

  private static boolean timingAttackSafeEqual(final byte[] expect, final byte[] actual) {
    if (expect.length != actual.length) {
      return false;
    }
    int sum = 0;
    for (int i = 0; i < expect.length; i++) {
      sum |= expect[i] ^ actual[i];
    }
    return sum == 0;
  }

  private static byte[] tryReadPayloadNextField(final ByteBuffer payloadBuf)
      throws BufferUnderflowException {
    if (payloadBuf.remaining() == 0) {
      return EMPTY_BYTES;
    }
    final short lengthOfNextField = payloadBuf.getShort();
    final byte[] fieldBytes = new byte[lengthOfNextField];
    payloadBuf.get(fieldBytes);
    return fieldBytes;
  }
  private final byte[] macKey;

  private final byte[] secretKey;

  private SecureTokenCodec(final byte[] macKey, final byte[] secretKey) {
    this.macKey = macKey;
    this.secretKey = secretKey;
  }

  /**
   * each field must <= 256 bytes, total max field count is 10
   * <p>
   * token encode using url safe base64
   */
  public String encode(final long expireTime, final List<byte[]> fields) {
    Preconditions.checkState(fields.size() <= MAX_FIELD_SIZE,
        "exceed max field size:" + MAX_FIELD_SIZE);

    for (final byte[] field : fields) {
      Preconditions.checkArgument(field.length <= 256, "field must <= 256 bytes");
    }

    /*
     * <pre>
     * 
     * token = base64UrlSafe(tokenBytes)
     * 
     * tokenBytes = ivCipherText || mac
     * 
     * ivCipherText = version || randomIv || cipherText
     * 
     * mac = hmac( macKey, ivCipherText )
     * 
     * payload = expireTime || len(field1) || field1 || len(field2) || field2 ...;
     * 
     * cipherText = AES/CBC/PKCS5Padding(secretKey, payload);
     * 
     * - version is 1 byte - len() function is 2 byte - expireTime is 8 bytes
     * 
     * </pre>
     */
    final byte[] randomIV = new byte[RANDOM_IV_LENGTH];
    LazyHolder.SECURE_RANDOM.nextBytes(randomIV);

    // System.out.println("encrypt: iv:" + Hex.encodeHexString(randomIV));

    int payloadLength = 8; // expire time long
    for (final byte[] field : fields) {
      payloadLength += (2 + field.length); // length byte + field bytes
    }

    final ByteBuffer pyaloadBuf = ByteBuffer.allocate(payloadLength);
    pyaloadBuf.putLong(expireTime); // 8

    for (final byte[] field : fields) {
      writePayloadNextField(pyaloadBuf, field);
    }

    pyaloadBuf.flip();

    // System.out.println("encrypt: model:" + Hex.encodeHexString((openApiAppId + uid +
    // uniqueDeviceId).getBytes()));

    final byte[] payload = new byte[pyaloadBuf.limit()];
    pyaloadBuf.get(payload);

    try {
      final byte[] cipherText = Encryptor.create(secretKey, randomIV).encrypt(payload);

      final ByteBuffer tokenBuf = ByteBuffer.allocate(VERSION_LENGTH
          + randomIV.length
          + cipherText.length
          + MAC_LENGTH);
      tokenBuf.put(VERSION_2);
      tokenBuf.put(randomIV);
      tokenBuf.put(cipherText);

      final byte[] ivCipherText = Arrays.copyOf(tokenBuf.array(), tokenBuf.capacity() - MAC_LENGTH);

      final byte[] mac = generateMacForIvCipherText(macKey, ivCipherText);
      tokenBuf.put(mac);

      return Encryptor.URL_SAFE_BASE64_ENCODER.encodeToString(tokenBuf.array());
    } catch (final GeneralSecurityException e) {
      throw new RuntimeException(e);
    }

  }

  public String encodeString(long expireTime, String... fields) {
    Preconditions.checkArgument(fields.length > 0, "at least one field is required");
    return encode(expireTime, Stream.of(fields).map(s -> s.getBytes(UTF8)).collect(toList()));
  }

  private ByteBuffer extractValidPayloadFields(final String targetToken) {
    if (targetToken == null) {
      return null;
    }

    final byte[] target = Base64.getUrlDecoder().decode(targetToken);
    if (target.length < MAC_LENGTH + RANDOM_IV_LENGTH) {
      return null;
    }

    final byte[] ivCipherText = Arrays.copyOf(target, target.length - MAC_LENGTH);
    final byte[] mac = Arrays.copyOfRange(target, target.length - MAC_LENGTH, target.length);

    /**
     * please valid check must be constant time, no matter how many branch in validations, and
     * it's not allow early quit either.
     */
    boolean valid = true;
    try {
      final byte[] actualMac = generateMacForIvCipherText(macKey, ivCipherText);
      valid &= timingAttackSafeEqual(actualMac, mac);
    } catch (final GeneralSecurityException e) {
      valid &= false;
    }

    final byte version = ivCipherText[0];

    valid &= version == VERSION_2;

    final int endOfRandomIv = VERSION_LENGTH + RANDOM_IV_LENGTH;
    final byte[] iv = Arrays.copyOfRange(ivCipherText, VERSION_LENGTH, endOfRandomIv);

    // System.out.println("decrypt: iv:" + Hex.encodeHexString(iv));

    final byte[] cipherText = Arrays.copyOfRange(ivCipherText, endOfRandomIv, ivCipherText.length);

    try {
      final byte[] payload = Decryptor.create(secretKey, iv).decrypt(cipherText);
      final ByteBuffer payloadBuf = ByteBuffer.wrap(payload);
      final long expireTime = payloadBuf.getLong();
      valid &= Instant.ofEpochMilli(expireTime).isAfter(Instant.now());
      if (valid) {
        return payloadBuf;
      }
    } catch (final GeneralSecurityException e) {
    }
    return null;
  }

  /**
   * return null if token is malformed or expired
   */
  public List<byte[]> tryDecode(final String targetToken) {
    final ByteBuffer payloadBuf = extractValidPayloadFields(targetToken);
    if (payloadBuf == null) {
      return null;
    }

    final List<byte[]> fields = new ArrayList<>();
    try {
      byte[] field = null;
      while ((field = tryReadPayloadNextField(payloadBuf)).length > 0) {
        fields.add(field);
        if (fields.size() > MAX_FIELD_SIZE) {
          return null;
        }
      }
      return fields;
    } catch (final BufferUnderflowException e) {
      return null;
    }
  }

  /**
   * try decode as String fields, this should only used when you encode String fields via
   * {@link #encodeString(long, String...)}.
   * <p>
   * you should not try to decode token that based on {@link #encode(long, java.util.List)} bytes
   * fields
   * (because byte fields are not always UTF-8 encoded bytes)
   */
  public List<String> tryDecodeAsString(String targetToken) {
    return Optional.ofNullable(tryDecode(targetToken))
        .map(fields -> fields.stream().map(bytes -> new String(bytes, UTF8)).collect(toList()))
        .orElse(null);
  }

  public boolean validateToken(final String targetToken, final List<byte[]> knownFields) {
    /**
     * please valid check must be constant time, no matter how many branch in validations, and
     * it's not allow early quit either.
     */
    boolean valid = true;
    final ByteBuffer payloadBuf = extractValidPayloadFields(targetToken);
    if (payloadBuf == null) {
      return false;
    }
    try {
      for (final byte[] knownField : knownFields) {
        final byte[] decodedField = tryReadPayloadNextField(payloadBuf);
        valid &= timingAttackSafeEqual(knownField, decodedField);
      }
      valid &= payloadBuf.remaining() == 0;
    } catch (final BufferUnderflowException e) {
      valid &= false;
    }
    return valid;
  }

  private void writePayloadNextField(final ByteBuffer pyaloadBuf, final byte[] nextFieldBytes) {
    pyaloadBuf.putShort((short) nextFieldBytes.length);
    pyaloadBuf.put(nextFieldBytes);
  }
}
