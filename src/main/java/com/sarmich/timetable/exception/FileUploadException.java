package com.sarmich.timetable.exception;

import com.sarmich.timetable.exception.handler.ErrorCode;


import com.sarmich.timetable.exception.handler.ExceptionInterface;


public class FileUploadException extends RuntimeException implements ExceptionInterface {
  private final ErrorCode code = ErrorCode.FILE_UPLOAD_ERROR_CODE;

  public FileUploadException() {}

  public FileUploadException(final String message) {
    super(message);
  }

  public FileUploadException(final String message, final Throwable cause) {
    super(message, cause);
  }

  @Override
  public ErrorCode getCode() {
    return code;
  }
}
