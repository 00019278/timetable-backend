package com.sarmich.timetable.exception;

import com.sarmich.timetable.exception.handler.ErrorCode;
import com.sarmich.timetable.exception.handler.ExceptionInterface;
import java.util.NoSuchElementException;
import lombok.Getter;

public class NotFoundException extends NoSuchElementException implements ExceptionInterface {
  private ErrorCode code = ErrorCode.NOT_FOUND_ERROR_CODE;
  @Getter private ExceptionResponse exceptionResponse;

  public NotFoundException(ErrorCode code) {
    super();
    this.code = code;
  }

  public NotFoundException(ExceptionResponse exceptionResponse) {
    super();
    this.exceptionResponse = exceptionResponse;
  }

  @Override
  public ErrorCode getCode() {
    return code;
  }

  public NotFoundException(final String message) {
    super(message);
  }

  public NotFoundException(final ErrorCode code, final String message) {
    super(message);
    this.code = code;
  }
}
