package io.kaif.token;

import static org.junit.Assert.*;

import java.util.Random;
import java.util.UUID;

import org.junit.Test;

public class BytesTest {

  @Test
  public void uuid() throws Exception {
    UUID uuid = UUID.randomUUID();
    byte[] bytes = Bytes.uuidToBytes(uuid);
    assertEquals(uuid, Bytes.uuidFromBytes(bytes));
  }

  @Test
  public void intBytes() throws Exception {
    int value = new Random().nextInt();
    byte[] bytes = Bytes.intToBytes(value);
    assertEquals(value, Bytes.intFromBytes(bytes));
  }

  @Test
  public void longBytes() throws Exception {
    long value = new Random().nextLong();
    byte[] bytes = Bytes.longToBytes(value);
    assertEquals(value, Bytes.longFromBytes(bytes));
  }
}