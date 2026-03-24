package com.sarmich.timetable.exp.exception;

public class FileRemoveException extends RuntimeException {

  public FileRemoveException() {}

  public FileRemoveException(final String message) {
    super(message);
  }

  public FileRemoveException(final String message, final Throwable cause) {
    super(message, cause);
  }
}
