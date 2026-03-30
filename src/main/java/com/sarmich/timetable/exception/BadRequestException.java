package com.sarmich.timetable.exception;

import com.sarmich.timetable.exception.handler.ErrorCode;
import com.sarmich.timetable.exception.handler.ExceptionInterface;
import lombok.Getter;

public class BadRequestException extends RuntimeException implements ExceptionInterface {
  private ErrorCode code = ErrorCode.BAD_REQUEST_CODE;
  @Getter private ExceptionResponse exceptionResponse;

  public BadRequestException() {}

  public BadRequestException(final String message) {
    super(message);
  }

  public BadRequestException(final ExceptionResponse exceptionResponse) {
    this.exceptionResponse = exceptionResponse;
  }

  public BadRequestException(final ErrorCode code, final String message) {
    super(message);
    this.code = code;
  }

  public BadRequestException(final ErrorCode code) {
    super();
    this.code = code;
  }

  public BadRequestException(final String message, final Throwable cause) {
    super(message, cause);
  }

  public BadRequestException(final Throwable cause) {
    super(cause);
  }

  public BadRequestException(
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
