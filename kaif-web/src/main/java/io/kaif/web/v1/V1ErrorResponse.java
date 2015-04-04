package io.kaif.web.v1;

import java.util.Collections;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * a oauth error response with body like:
 * <p>
 * <pre>
 * {
 *    "errors":[
 *      {
 *        "status": 400,
 *        "title": "bad request"
 *      }
 *    ]
 * }
 * </pre>
 * <p>
 */
public class V1ErrorResponse {

  @JsonPropertyOrder(value = { "status", "title" })
  public static class Error {
    private final int status;
    private final String title;

    public Error(int status, String title) {
      this.status = status;
      this.title = title;
    }

    public int getStatus() {
      return status;
    }

    public String getTitle() {
      return title;
    }

    /**
     * although toString() currently return a valid JSON form of information, you should not rely
     * on
     * this format to do any parse or serialization. use Jackson to de/serialize object instead.
     */
    @Override
    public String toString() {
      return "{\"status\":" + status + ",\"title\":\"" + title + "\"}";
    }
  }
  private static final long serialVersionUID = 4886331307L;
  private final List<Error> errors;

  public V1ErrorResponse(final int status, final String title) {
    this.errors = Collections.singletonList(new Error(status, title));
  }

  public List<Error> getErrors() {
    return errors;
  }
}