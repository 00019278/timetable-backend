package com.sarmich.timetable.exception;

import com.sarmich.timetable.exception.handler.ErrorCode;


import com.sarmich.timetable.exception.handler.ExceptionInterface;


public class ApiException extends RuntimeException implements ExceptionInterface {
  private ErrorCode code;

  public ApiException(final String message, final ErrorCode errorCode) {
    super(message);
    this.code = errorCode;
  }

  @Override
  public ErrorCode getCode() {
    return code;
  }
}
