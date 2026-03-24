package com.sarmich.timetable.exp.exception;

public class BadRequestException extends RuntimeException {
  public BadRequestException() {}

  public BadRequestException(String message) {
    super(message);
  }

  public BadRequestException(Throwable cause) {
    super(cause);
  }

  public BadRequestException(final String message, final Throwable cause) {
    super(message, cause);
  }

  @Override
  public String toString() {
    return "BadRequestException: " + getMessage();
  }
}
