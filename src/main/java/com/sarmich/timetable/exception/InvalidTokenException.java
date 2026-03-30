package com.sarmich.timetable.exception;


import com.sarmich.timetable.exception.handler.ErrorCode;

public class InvalidTokenException extends UnauthorizedException {
  private final ErrorCode code = ErrorCode.INVALID_TOKEN_ERROR_CODE;

  @Override
  public ErrorCode getCode() {
    return code;
  }
}
