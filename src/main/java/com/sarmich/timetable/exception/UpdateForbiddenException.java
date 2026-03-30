package com.sarmich.timetable.exception;

import com.sarmich.timetable.exception.handler.ErrorCode;

public class UpdateForbiddenException extends ForbiddenException {
  private ErrorCode code = ErrorCode.UPDATE_FORBIDDEN_ERROR_CODE;

  public UpdateForbiddenException(final ErrorCode code) {
    super(code);
  }

  public ErrorCode getCode() {
    return code;
  }

  public UpdateForbiddenException(final String message) {
    super(message);
  }

  public UpdateForbiddenException(final ErrorCode code, final String message) {
    super(code, message);
    this.code = code;
  }
}
