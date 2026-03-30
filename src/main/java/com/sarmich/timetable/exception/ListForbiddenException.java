package com.sarmich.timetable.exception;

import com.sarmich.timetable.exception.handler.ErrorCode;


import com.sarmich.timetable.exception.handler.ExceptionInterface;


public class ListForbiddenException extends ForbiddenException implements ExceptionInterface {
  private final ErrorCode code = ErrorCode.LIST_FORBIDDEN_ERROR_CODE;

  @Override
  public ErrorCode getCode() {
    return code;
  }

  public ListForbiddenException() {}

  public ListForbiddenException(final String message) {
    super(message);
  }

  public ListForbiddenException(final String message, final Throwable cause) {
    super(message, cause);
  }

  public ListForbiddenException(final Throwable cause) {
    super(cause);
  }
}
