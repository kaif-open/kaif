package io.kaif.model.zone;

import java.io.IOException;
import java.util.Optional;
import java.util.regex.Pattern;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

@JsonSerialize(using = ZoneSerializer.class)
@JsonDeserialize(using = ZoneDeserializer.class)
public class Zone {
  /**
   * - must start with az09, end with az09, no dash
   * - must use dash to separate
   * - 3~30 chars.
   * - not allow concat multiple dash (use code to validate, not regex)
   * <p>
   * change pattern should review route.dart and ZoneController.java
   */
  private static final Pattern ZONE_PATTERN = Pattern.compile("^[a-z0-9][a-z0-9\\-]{1,28}[a-z0-9]$");

  private static String valueFallback(String rawValue) {
    if (Strings.isNullOrEmpty(rawValue)) {
      return null;
    }
    return rawValue.toLowerCase().replaceAll("[\\-_]+", "-");
  }

  private static boolean validateZone(String zone) {
    return zone != null
        && ZONE_PATTERN.matcher(zone).matches()
        && !zone.contains("--")
        && !zone.equals("null");
  }

  /**
   * fallback to valid zone name whenever possible (user is easily typo)
   * <p>
   * fallback rule is follow valid zone pattern
   */
  public static Optional<Zone> tryFallback(String rawZone) {
    return Optional.ofNullable(valueFallback(rawZone)).filter(Zone::validateZone).map(Zone::new);
  }

  public static Zone valueOf(String validValue) {
    Preconditions.checkArgument(validateZone(validValue), "invalid zone value: %s", validValue);
    return new Zone(validValue);
  }

  private final String value;

  private Zone(String value) {
    this.value = value;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    Zone zone = (Zone) o;
    return value.equals(zone.value);
  }

  @Override
  public int hashCode() {
    return value.hashCode();
  }

  public String value() {
    return value;
  }

  @Override
  public String toString() {
    return value;
  }
}

class ZoneSerializer extends JsonSerializer<Zone> {

  @Override
  public void serialize(Zone zone, JsonGenerator jgen, SerializerProvider provider)
      throws IOException, JsonProcessingException {
    jgen.writeString(zone.value());
  }

}

class ZoneDeserializer extends JsonDeserializer<Zone> {

  @Override
  public Zone deserialize(JsonParser jp, DeserializationContext ctxt)
      throws IOException, JsonProcessingException {
    String value = jp.readValueAs(String.class);
    return Zone.valueOf(value);
  }
}
