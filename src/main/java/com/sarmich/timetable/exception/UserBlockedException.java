package com.sarmich.timetable.exception;

import com.sarmich.timetable.exception.handler.ErrorCode;
import com.sarmich.timetable.exception.handler.ExceptionInterface;

public class UserBlockedException extends ForbiddenException implements ExceptionInterface {

  private ErrorCode code = ErrorCode.USER_BLOCKED_ERROR_CODE;

  public UserBlockedException(final ErrorCode code) {
    super();
    this.code = code;
  }

  @Override
  public ErrorCode getCode() {
    return code;
  }

  public UserBlockedException(final String message) {
    super(message);
  }

  public UserBlockedException(final ErrorCode code, final String message) {
    super(message);
    this.code = code;
  }
}
