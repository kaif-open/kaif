package io.kaif.web.api;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import io.kaif.web.support.ErrorResponse;

/**
 * a general error response with body like:
 * <p>
 * <pre>
 * {
 *    "code': 500,
 *    "reason": "Could not read file"
 * }
 * </pre>
 * <p>
 */
@JsonPropertyOrder(value = { "code", "reason" })
public class SimpleErrorResponse implements ErrorResponse {
  private static final long serialVersionUID = 488633136099878207L;
  private final int code;
  private final String reason;

  public SimpleErrorResponse(final int code, final String reason) {
    this.code = code;
    this.reason = reason;
  }

  public int getCode() {
    return code;
  }

  public String getReason() {
    return reason;
  }

  /**
   * although toString() currently return a valid JSON form of information, you should not rely on
   * this format to do any parse or serialization. use Jackson to de/serialize object instead.
   */
  @Override
  public String toString() {
    return "{\"code\":" + code + ",\"reason\":\"" + reason + "\"}";
  }
}