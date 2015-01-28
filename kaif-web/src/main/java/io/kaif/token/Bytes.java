package io.kaif.token;

import java.nio.ByteBuffer;
import java.util.UUID;

public class Bytes {
  public static UUID uuidFromBytes(byte[] raw) {
    ByteBuffer bb = ByteBuffer.wrap(raw);
    return new UUID(bb.getLong(), bb.getLong());
  }

  public static byte[] uuidToBytes(UUID uuid) {
    ByteBuffer bb = ByteBuffer.wrap(new byte[16]);
    bb.putLong(uuid.getMostSignificantBits());
    bb.putLong(uuid.getLeastSignificantBits());
    return bb.array();
  }

  public static int intFromBytes(byte[] raw) {
    return ByteBuffer.wrap(raw).getInt();
  }

  public static byte[] intToBytes(int value) {
    ByteBuffer bb = ByteBuffer.wrap(new byte[4]);
    bb.putInt(value);
    return bb.array();
  }

  public static byte[] longToBytes(long value) {
    ByteBuffer bb = ByteBuffer.wrap(new byte[8]);
    bb.putLong(value);
    return bb.array();
  }

  public static long longFromBytes(byte[] raw) {
    return ByteBuffer.wrap(raw).getLong();
  }
}

