package src.enums;

public enum StatusCode {
  OK(200, "OK"),
  UNAUTHORIZED(401, "Unauthorized"),
  FOUND(302, "Found"),
  BAD_REQUEST(400, "Bad Request"),
  NOT_FOUND(404, "Not Found"),
  INTERNAL_SERVER_ERROR(500, "Internal Server Error");

  private final int code;
  private final String phrase;

  StatusCode(int code, String phrase) {
    this.code = code;
    this.phrase = phrase;
  }

  public int getCode() {
    return this.code;
  }

  public String getPhrase() {
    return this.phrase;
  }
}
