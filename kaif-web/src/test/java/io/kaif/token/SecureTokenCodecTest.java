package io.kaif.token;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.*;
import static org.junit.Assert.*;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.IntStream;

import org.junit.Test;

public class SecureTokenCodecTest {
  private final byte[] macKey = { 12, 23, 31, 41, 5, 16, -7, -8, -9, -10, -11, 112, -11, 14, 15,
      16 };

  private final byte[] secretKey = { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16 };

  private void assertFieldsEquals(final List<byte[]> expect, final List<byte[]> actual) {
    if (actual == null && expect == null) {
      return;
    }
    if (actual == null && expect != null) {
      fail("expect fields size is " + expect.size() + ", but was null");
    }
    if (actual != null && expect == null) {
      fail("expect null, but was fields size:" + actual.size());
    }
    if (actual.size() != expect.size()) {
      fail("expect fields size is " + expect.size() + ", but was " + actual.size());
    }
    for (int i = 0; i < expect.size(); i++) {
      final byte[] expectByte = expect.get(i);
      final byte[] actualByte = actual.get(i);
      assertArrayEquals(expectByte, actualByte);
    }
  }

  @Test
  public void codec() throws Exception {
    final SecureTokenCodec codec = SecureTokenCodec.create(macKey, secretKey);
    final String field1 = UUID.randomUUID().toString();
    final String field2 = UUID.randomUUID().toString();
    final String field3 = UUID.randomUUID().toString();

    Instant expireTime = Instant.now().plus(Duration.ofDays(356 * 20));
    final List<byte[]> fields = Arrays.asList(field1.getBytes(),
        field2.getBytes(),
        field3.getBytes());
    final String token = codec.encode(expireTime.toEpochMilli(), fields);

    assertTrue(token.length() > 160);
    assertTrue(codec.validateToken(token, fields));
    final List<byte[]> decodedFields = codec.tryDecode(token);
    assertFieldsEquals(fields, decodedFields);
  }

  @Test
  public void codec_more() throws Exception {
    final HashSet<String> allTokens = new HashSet<>();
    final int testCount = 10000;
    for (int i = 0; i < testCount; i++) {
      final SecureTokenCodec codec = SecureTokenCodec.create(macKey, secretKey);
      final String field1 = UUID.randomUUID().toString();
      final String field2 = UUID.randomUUID().toString();
      final String field3 = UUID.randomUUID().toString();

      Instant expireTime = Instant.now().plus(Duration.ofDays(356 * 20));

      final List<byte[]> fields = Arrays.asList(field1.getBytes(),
          field2.getBytes(),
          field3.getBytes());
      final String token = codec.encode(expireTime.toEpochMilli(), fields);

      allTokens.add(token);
      assertTrue(token.length() > 160);
      assertTrue(codec.validateToken(token, fields));
      final List<byte[]> decodedFields = codec.tryDecode(token);
      assertFieldsEquals(fields, decodedFields);
    }
    assertEquals(10000, allTokens.size());
  }

  @Test
  public void codec_more_fields() throws Exception {
    final byte[] macKey = { 12, 23, 31, 41, 5, 16, -7, -8, -9, -10, -11 };

    final byte[] secretKey = { 16, 1, 2, 3, 4, 5, -6, 7, 8, 9, -10, 11, 12, 13, -14, 15, };
    final SecureTokenCodec codec = SecureTokenCodec.create(macKey, secretKey);

    final List<byte[]> fields = new ArrayList<>();
    for (int i = 0; i < 10; i++) {
      fields.add(UUID.randomUUID().toString().getBytes());
    }

    Instant expireTime = Instant.now().plus(Duration.ofDays(356 * 20));

    final String token = codec.encode(expireTime.toEpochMilli(), fields);

    assertTrue(codec.validateToken(token, fields));
    final List<byte[]> decodedFields = codec.tryDecode(token);
    assertFieldsEquals(fields, decodedFields);
  }

  @Test
  public void codecString() throws Exception {
    final SecureTokenCodec codec = SecureTokenCodec.create(macKey, secretKey);
    final String field1 = UUID.randomUUID().toString();
    final String field2 = UUID.randomUUID().toString();
    final String field3 = UUID.randomUUID().toString();

    Instant expireTime = Instant.now().plus(Duration.ofDays(356 * 20));

    final List<byte[]> byteFields = asList(field1.getBytes(), field2.getBytes(), field3.getBytes());
    final String token = codec.encodeString(expireTime.toEpochMilli(), field1, field2, field3);

    assertTrue(token.length() > 160);
    assertTrue(codec.validateToken(token, byteFields));
    final List<String> decodedFields = codec.tryDecodeAsString(token);
    assertFieldsEquals(byteFields, decodedFields.stream().map(s -> s.getBytes()).collect(toList()));
  }

  @Test
  public void codecString_long_field() throws Exception {
    final SecureTokenCodec codec = SecureTokenCodec.create(macKey, secretKey);
    final String field1 = IntStream.range(0, 256).mapToObj(i -> "a").collect(joining());
    final String field2 = UUID.randomUUID().toString();

    Instant expireTime = Instant.now().plus(Duration.ofDays(356 * 20));

    final List<byte[]> byteFields = asList(field1.getBytes(), field2.getBytes());
    final String token = codec.encodeString(expireTime.toEpochMilli(), field1, field2);

    assertTrue(token.length() > 160);
    assertTrue(codec.validateToken(token, byteFields));
    final List<String> decodedFields = codec.tryDecodeAsString(token);
    assertFieldsEquals(byteFields, decodedFields.stream().map(s -> s.getBytes()).collect(toList()));
  }

  @Test
  public void generateRandomSecretKey() throws Exception {
    final Set<String> keys = IntStream.range(0, 10000).mapToObj(i -> {
      return SecureTokenCodec.generateUrlSafeKey();
    }).collect(toSet());

    assertEquals(10000, keys.size());
    assertEquals(10000,
        keys.stream()
            .map(SecureTokenCodec::convertUrlSafeKeyToBytes)
            .filter(bytes -> bytes.length == 16)
            .count());
  }

  @Test
  public void tryDecode_expired() throws Exception {
    final SecureTokenCodec codec = SecureTokenCodec.create(macKey, secretKey);
    final String field1 = "123412341234adfa";
    final String field2 = "134dfasdfasdfa";

    Instant expire = Instant.now().minus(Duration.ofMinutes(1));

    final List<byte[]> fields = Arrays.asList(field1.getBytes(), field2.getBytes());
    final String token = codec.encode(expire.toEpochMilli(), fields);
    assertNull(codec.tryDecode(token));
  }

  @Test
  public void tryDecode_failed() throws Exception {
    final SecureTokenCodec codec = SecureTokenCodec.create(macKey, secretKey);
    assertNull(codec.tryDecode(
        "qSruSGAIYINH3y461ZjQX3xM08nCPXKp10punG7t15W6ixNivb5aSZUlY5XapiamKY8PsETguAHV4AFoOZx7DSjdEcjqqmrtZqgiXgZgNR3LgujbpBuO1mejxcq3HAS"));
  }

  @Test
  public void tryDecode_failed_invalid_base64() throws Exception {
    final SecureTokenCodec codec = SecureTokenCodec.create(macKey, secretKey);
    assertNull(codec.tryDecode(
        "Ahde3Wy_r4JG0fhsH_NAvZ9StbzAk6nIDUCP_FcTOkEz1QqWVCPStlCpgXEy8NVgzbWOGHzha6enVThKuwlB2dg8SSa1Bme3P6Mh6t7x4tA9SU6sA"));
  }
}
