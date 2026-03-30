package com.sarmich.timetable.exception;

public class UnauthorizedExceptionYandex extends UnauthorizedException {

  public UnauthorizedExceptionYandex() {}

  public UnauthorizedExceptionYandex(final String message) {
    super(message);
  }

  public UnauthorizedExceptionYandex(final String message, final Throwable cause) {
    super(message, cause);
  }

  public UnauthorizedExceptionYandex(final Throwable cause) {
    super(cause);
  }
}
