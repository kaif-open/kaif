package io.kaif.web.v1;

import java.util.Collections;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import io.kaif.web.support.ErrorResponse;

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
public class V1ErrorResponse implements ErrorResponse {

  @JsonPropertyOrder(value = { "status", "title" })
  public static class Error {
    private final int status;
    private final String title;
    //optional
    private final String type;
    //optional
    private final Boolean translated;

    public Error(int status, String title, String type, Boolean translated) {
      this.status = status;
      this.title = title;
      this.type = type;
      this.translated = translated;
    }

    public Error(int status, String title) {
      this(status, title, null, null);
    }

    public int getStatus() {
      return status;
    }

    public String getTitle() {
      return title;
    }

    public String getType() {
      return type;
    }

    public Boolean isTranslated() {
      return translated;
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
    this(status, title, null, null);
  }

  public V1ErrorResponse(final int status,
      final String title,
      final String type,
      final Boolean translated) {
    this.errors = Collections.singletonList(new Error(status, title, type, translated));
  }

  public List<Error> getErrors() {
    return errors;
  }
}