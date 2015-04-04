package io.kaif.web.support;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

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
public class RestErrorResponse {
  private static final long serialVersionUID = 488633136099878207L;
  private final int code;
  private final String reason;

  public RestErrorResponse(final int code, final String reason) {
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