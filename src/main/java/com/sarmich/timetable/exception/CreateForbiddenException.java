package com.sarmich.timetable.exception;

import com.sarmich.timetable.exception.handler.ErrorCode;

public class CreateForbiddenException extends ForbiddenException {
  private ErrorCode code = ErrorCode.CREATE_FORBIDDEN_ERROR_CODE;

  @Override
  public ErrorCode getCode() {
    return code;
  }

  public CreateForbiddenException() {}

  public CreateForbiddenException(final String message) {
    super(message);
  }

  public CreateForbiddenException(final ErrorCode code, final String message) {
    super(message);
    this.code = code;
  }

  public CreateForbiddenException(final ErrorCode code) {
    super();
    this.code = code;
  }

  public CreateForbiddenException(final String message, final Throwable cause) {
    super(message, cause);
  }

  public CreateForbiddenException(final Throwable cause) {
    super(cause);
  }

  public CreateForbiddenException(
      final String message,
      final Throwable cause,
      final boolean enableSuppression,
      final boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }
}
