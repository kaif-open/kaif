package io.kaif.web.support;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

/**
 * <p>
 * a convenient wrapper when you return single value in JSON. Example use cases are:
 * </p>
 * <p>
 * 1. your method return an integer:
 * <p>
 * <pre>
 * public SingleWrapper&lt;Integer&gt; count() {
 *   return SingleWrapper.of(&quot;count&quot;, 12);
 * }
 *
 * // return is like {&quot;count&quot;:12}
 * </pre>
 * <p>
 * 2. your method return a String, this case is most useful because if not using wrapper, string
 * returned will not be double quoted even using application/json content-type
 * <p>
 * <pre>
 * public SingleWrapper&lt;String&gt; mark() {
 *   return SingleWrapper.of(&quot;correct&quot;);
 * }
 *
 * // return is like {&quot;data&quot;:&quot;correct&quot;}
 * </pre>
 * <p>
 * 3. your method return a single nullable POJO, not collection. if the POJO is null, the response
 * will become empty body (0 byte). this make parsing harder and client don't know what the
 * meaning of empty response body (success? fail?).
 * <p>
 * <pre>
 * public SingleWrapper&lt;Pojo&gt; findByXxx() {
 *   return SingleWrapper.of(null);
 * }
 *
 * // return is like {&quot;data&quot;:null}
 * </pre>
 * <p>
 * of course, you don't need to apply SingleWrapper to a POJO that never be null.
 */
@JsonSerialize(using = SingleWrapperSerializer.class)
public class SingleWrapper<T> {

  /**
   * wrapped value in "data" field
   * <p>
   * <pre>
   * {
   *   "data":value
   * }
   * </pre>
   */
  public static <T> SingleWrapper<T> of(T value) {
    return new SingleWrapper<>("data", value);
  }

  /**
   * wrapped value in custom field name
   * <p>
   * <pre>
   * {
   *   "myFieldName":value
   * }
   * </pre>
   */
  public static <T> SingleWrapper<T> of(T value, String fieldName) {
    return new SingleWrapper<>(fieldName, value);
  }

  private static final long serialVersionUID = 68220190832499715L;

  private final String fieldName;

  private final T value;

  private SingleWrapper(String fieldName, T value) {
    this.fieldName = fieldName;
    this.value = value;
  }

  public String getFieldName() {
    return fieldName;
  }

  public T getValue() {
    return value;
  }

}

class SingleWrapperSerializer extends JsonSerializer<SingleWrapper<?>> {

  @Override
  public void serialize(SingleWrapper<?> value, JsonGenerator jgen, SerializerProvider provider)
      throws IOException, JsonProcessingException {
    jgen.writeStartObject();
    jgen.writeObjectField(value.getFieldName(), value.getValue());
    jgen.writeEndObject();
  }

}