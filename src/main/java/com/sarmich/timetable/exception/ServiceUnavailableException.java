package com.sarmich.timetable.exception;

import com.sarmich.timetable.exception.handler.ErrorCode;


import com.sarmich.timetable.exception.handler.ExceptionInterface;


public class ServiceUnavailableException extends RuntimeException implements ExceptionInterface {
  private final ErrorCode code = ErrorCode.SERVICE_UNAVAILABLE_ERROR_CODE;

  public ServiceUnavailableException() {}

  public ServiceUnavailableException(final String message) {
    super(message);
  }

  public ServiceUnavailableException(final String message, final Throwable cause) {
    super(message, cause);
  }

  public ServiceUnavailableException(final Throwable cause) {
    super(cause);
  }

  @Override
  public ErrorCode getCode() {
    return code;
  }
}
