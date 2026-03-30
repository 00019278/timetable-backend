package com.sarmich.timetable.exception;

import com.sarmich.timetable.exception.handler.ErrorCode;
import com.sarmich.timetable.exception.handler.ExceptionInterface;

public class FileRemoveException extends RuntimeException implements ExceptionInterface {
  private final ErrorCode code = ErrorCode.FILE_REMOVE_ERROR_CODE;

  public FileRemoveException() {}

  public FileRemoveException(final String message) {
    super(message);
  }

  public FileRemoveException(final String message, final Throwable cause) {
    super(message, cause);
  }

  @Override
  public ErrorCode getCode() {
    return code;
  }
}
