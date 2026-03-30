package com.sarmich.timetable.exception;

import com.sarmich.timetable.exception.handler.ErrorCode;



public class DeleteForbiddenException extends ForbiddenException {

  private ErrorCode code = ErrorCode.DELETE_FORBIDDEN_ERROR_CODE;

  public DeleteForbiddenException() {}

  public DeleteForbiddenException(final String message) {
    super(message);
  }

  public DeleteForbiddenException(final ErrorCode code, final String message) {
    super(message);
    this.code = code;
  }

  public DeleteForbiddenException(final ErrorCode code) {
    super();
    this.code = code;
  }

  public DeleteForbiddenException(final String message, final Throwable cause) {
    super(message, cause);
  }

  public DeleteForbiddenException(final Throwable cause) {
    super(cause);
  }

  public DeleteForbiddenException(
      final String message,
      final Throwable cause,
      final boolean enableSuppression,
      final boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }

  @Override
  public ErrorCode getCode() {
    return code;
  }
}
