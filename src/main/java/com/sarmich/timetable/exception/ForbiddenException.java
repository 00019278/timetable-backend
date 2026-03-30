package com.sarmich.timetable.exception;

import com.sarmich.timetable.exception.handler.ErrorCode;
import com.sarmich.timetable.exception.handler.ExceptionInterface;
import lombok.Getter;

public class ForbiddenException extends RuntimeException implements ExceptionInterface {
  private ErrorCode code = ErrorCode.FORBIDDEN_ERROR_CODE;
  @Getter private ExceptionResponse exceptionResponse;

  public ForbiddenException() {}

  public ForbiddenException(final ErrorCode code) {
    this.code = code;
  }

  public ForbiddenException(final String message) {
    super(message);
  }

  public ForbiddenException(final ExceptionResponse exceptionResponse) {
    this.exceptionResponse = exceptionResponse;
  }

  public ForbiddenException(final ErrorCode code, final String message) {
    super(message);
    this.code = code;
  }

  public ForbiddenException(final String message, final Throwable cause) {
    super(message, cause);
  }

  public ForbiddenException(final Throwable cause) {
    super(cause);
  }

  public ForbiddenException(
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
