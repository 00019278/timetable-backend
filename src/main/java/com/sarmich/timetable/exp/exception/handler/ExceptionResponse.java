package com.sarmich.timetable.exp.exception.handler;

import com.sarmich.timetable.exp.exception.AlreadyExistsException;
import com.sarmich.timetable.exp.exception.*;
import org.springframework.http.HttpStatus;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.NoSuchElementException;

public record ExceptionResponse(
    int code, String status, String path, String message, String timestamp) {

  public ExceptionResponse(final Exception exception, final String path, final HttpStatus status) {
    this(
        findErrorCode(exception),
        String.format("%d %s", status.value(), status.getReasonPhrase()),
        path,
        exception.getMessage(),
        DateTimeFormatter.ofLocalizedDateTime(FormatStyle.LONG)
            .withZone(ZoneId.of("UTC+5"))
            .format(Instant.now()));
  }

  private static int findErrorCode(final Exception e) {
    if (e instanceof ApiException) {
      return ErrorCodes.API_ERROR_CODE;
    }

    if (e instanceof AlreadyExistsException) {
      return ErrorCodes.ALREADY_EXISTS_ERROR_CODE;
    }

    if (e instanceof NoSuchElementException) {
      return ErrorCodes.NOT_FOUND_ERROR_CODE;
    }

    if (e instanceof NullPointerException) {
      return ErrorCodes.NULL_POINTER_ERROR_CODE;
    }

    if (e instanceof UnsupportedOperationException) {
      return ErrorCodes.UNSUPPORTED_OPERATION_ERROR_CODE;
    }

    if (e instanceof InvalidArgumentException) {
      return ErrorCodes.INVALID_ARGUMENT_ERROR_CODE;
    }

    if (e instanceof IllegalArgumentException) {
      return ErrorCodes.INVALID_ARGUMENT_ERROR_CODE;
    }

    if (e instanceof InvalidCredentialsException) {
      return ErrorCodes.INVALID_CREDENTIALS_ERROR_CODE;
    }

    if (e instanceof InvalidTokenException) {
      return ErrorCodes.INVALID_TOKEN_ERROR_CODE;
    }

    if (e instanceof InvalidOperationException) {
      return ErrorCodes.INVALID_OPERATION_ERROR_CODE;
    }

    if (e instanceof LockingConflictException) {
      return ErrorCodes.LOCKING_CONFLICT_ERROR_CODE;
    }

    if (e instanceof UnauthorizedException) {
      return ErrorCodes.UNAUTHORIZED_ERROR_CODE;
    }

    if (e instanceof CreateForbiddenException) {
      return ErrorCodes.CREATE_FORBIDDEN_ERROR_CODE;
    }

    if (e instanceof DeleteForbiddenException) {
      return ErrorCodes.DELETE_FORBIDDEN_ERROR_CODE;
    }

    if (e instanceof ListForbiddenException) {
      return ErrorCodes.LIST_FORBIDDEN_ERROR_CODE;
    }

    if (e instanceof RetrieveForbiddenException) {
      return ErrorCodes.RETRIEVE_FORBIDDEN_ERROR_CODE;
    }

    if (e instanceof UpdateForbiddenException) {
      return ErrorCodes.UPDATE_FORBIDDEN_ERROR_CODE;
    }

    if (e instanceof UserBlockedException) {
      return ErrorCodes.USER_BLOCKED_ERROR_CODE;
    }

    if (e instanceof ForbiddenException) {
      return ErrorCodes.FORBIDDEN_ERROR_CODE;
    }

    if (e instanceof JsonParsingException) {
      return ErrorCodes.JSON_PARSING_ERROR_CODE;
    }

    if (e instanceof FileDownloadException) {
      return ErrorCodes.FILE_DOWNLOAD_ERROR_CODE;
    }

    if (e instanceof FileRemoveException) {
      return ErrorCodes.FILE_REMOVE_ERROR_CODE;
    }

    if (e instanceof FileUploadException) {
      return ErrorCodes.FILE_UPLOAD_ERROR_CODE;
    }

    if (e instanceof TimeoutException) {
      return ErrorCodes.TIMEOUT_ERROR_CODE;
    }

    if (e instanceof ServiceUnavailableException) {
      return ErrorCodes.SERVICE_UNAVAILABLE_ERROR_CODE;
    }

    return ErrorCodes.INTERNAL_ERROR_CODE;
  }
}
