package com.sarmich.timetable.exception;

import com.sarmich.timetable.exception.handler.ErrorCode;


import com.sarmich.timetable.exception.handler.ExceptionInterface;


public class FileDownloadException extends RuntimeException implements ExceptionInterface {
  private final ErrorCode code = ErrorCode.FILE_DOWNLOAD_ERROR_CODE;

  public FileDownloadException() {}

  public FileDownloadException(final String message) {
    super(message);
  }

  public FileDownloadException(final String message, final Throwable cause) {
    super(message, cause);
  }

  @Override
  public ErrorCode getCode() {
    return code;
  }
}
