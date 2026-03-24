package com.sarmich.timetable.exp.exception;

public class DocumentException extends RuntimeException {

  public DocumentException() {}

  public DocumentException(final String message) {
    super(message);
  }

  public DocumentException(final String message, final Throwable cause) {
    super(message, cause);
  }
}
