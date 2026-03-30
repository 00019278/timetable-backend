package com.sarmich.timetable.exception;

import com.sarmich.timetable.exception.handler.ErrorCode;
import com.sarmich.timetable.exception.handler.ExceptionInterface;

public class AlreadyExistsException extends RuntimeException implements ExceptionInterface {
  private ErrorCode code = ErrorCode.ALREADY_EXISTS_ERROR_CODE;

  public AlreadyExistsException(final ErrorCode code) {
    this.code = code;
  }

  public ErrorCode getCode() {
    return code;
  }

  public AlreadyExistsException(final String message) {
    super(message);
  }

  public AlreadyExistsException(final ErrorCode code, final String message) {
    super(message);
    this.code = code;
  }
}
