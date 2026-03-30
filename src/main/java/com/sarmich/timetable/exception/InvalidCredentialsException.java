package com.sarmich.timetable.exception;

import com.sarmich.timetable.exception.handler.ErrorCode;
import com.sarmich.timetable.exception.handler.ExceptionInterface;

public class InvalidCredentialsException extends UnauthorizedException
    implements ExceptionInterface {

  private ErrorCode code = ErrorCode.INVALID_CREDENTIALS_ERROR_CODE;

  public InvalidCredentialsException(ErrorCode code) {
    this.code = code;
  }

  @Override
  public ErrorCode getCode() {
    return code;
  }

  public InvalidCredentialsException(final String message) {
    super(message);
  }

  public InvalidCredentialsException(final ErrorCode code, final String message) {
    super(message);
    this.code = code;
  }
}
