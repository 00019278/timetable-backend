package com.sarmich.timetable.exception.handler;

import static org.springframework.http.HttpStatus.*;

import com.sarmich.timetable.exception.*;
import com.sarmich.timetable.model.common.Response;
import com.sarmich.timetable.utils.Util;
import java.io.IOException;
import java.time.format.DateTimeParseException;
import java.util.Locale;
import java.util.NoSuchElementException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.core.codec.DecodingException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@ControllerAdvice
public class GlobalExceptionHandler {
  final ResourceBundleMessageSource messageSource;

  private static final Logger LOGGER = LogManager.getLogger();

  public GlobalExceptionHandler(ResourceBundleMessageSource messageSource) {
    this.messageSource = messageSource;
  }

  @ResponseStatus(BAD_REQUEST)
  @ExceptionHandler({MethodArgumentNotValidException.class})
  public final ResponseEntity<?> handleException(
      final MethodArgumentNotValidException e, final WebRequest request) {
    return constructExceptionResponse(e, request, BAD_REQUEST, ErrorCode.REQUIRED_FIELD_MISSED);
  }

  @ResponseStatus(BAD_REQUEST)
  @ExceptionHandler({MissingServletRequestParameterException.class})
  public final ResponseEntity<?> handleException(
      final MissingServletRequestParameterException e, final WebRequest request) {
    return constructExceptionResponse(e, request, BAD_REQUEST, ErrorCode.REQUIRED_FIELD_MISSED);
  }

  @ResponseStatus(BAD_REQUEST)
  @ExceptionHandler({HttpMessageNotReadableException.class})
  public final ResponseEntity<?> handleException(
      final HttpMessageNotReadableException e, final WebRequest request) {
    if (e.getCause().getCause() instanceof final InvalidArgumentException cause) {
      return constructExceptionResponse(e, request, BAD_REQUEST, cause.getCode());
    }
    return constructExceptionResponse(e, request, BAD_REQUEST, ErrorCode.REQUIRED_FIELD_MISSED);
  }

  @ResponseStatus(INTERNAL_SERVER_ERROR)
  @ExceptionHandler({Exception.class, RuntimeException.class})
  public final ResponseEntity<?> handleException(final Exception e, final WebRequest request) {
    return constructExceptionResponse(
        e, request, INTERNAL_SERVER_ERROR, ErrorCode.INTERNAL_ERROR_CODE);
  }

  @ResponseStatus(INTERNAL_SERVER_ERROR)
  @ExceptionHandler(IOException.class)
  public ResponseEntity<?> handleIOException(final IOException e, final WebRequest request) {
    return constructExceptionResponse(e, request, INTERNAL_SERVER_ERROR, ErrorCode.IO_EXCEPTION);
  }

  @ResponseStatus(BAD_REQUEST)
  @ExceptionHandler({InvalidOperationException.class})
  public ResponseEntity<?> handleBadRequests(
      final InvalidOperationException e, final WebRequest request) {
    return constructExceptionResponse(e, request, BAD_REQUEST, e.getCode());
  }

  @ResponseStatus(PAYLOAD_TOO_LARGE)
  @ExceptionHandler({MaxUploadSizeExceededException.class})
  public ResponseEntity<?> handleBadRequests(
      final MaxUploadSizeExceededException e, final WebRequest request) {
    return constructExceptionResponse(
        e, request, PAYLOAD_TOO_LARGE, ErrorCode.MAX_UPLOAD_SIZE_EXCEEDED);
  }

  @ResponseStatus(BAD_REQUEST)
  @ExceptionHandler({
    InvalidArgumentException.class,
  })
  public ResponseEntity<?> handleBadRequests(
      final InvalidArgumentException e, final WebRequest request) {
    return constructExceptionResponse(e, request, BAD_REQUEST, e.getCode());
  }

  @ResponseStatus(BAD_REQUEST)
  @ExceptionHandler({
    MethodArgumentTypeMismatchException.class,
  })
  public ResponseEntity<?> handleBadRequests(
      final MethodArgumentTypeMismatchException e, final WebRequest request) {
    return constructExceptionResponse(e, request, BAD_REQUEST, ErrorCode.INVALID_TYPE);
  }

  @ResponseStatus(BAD_REQUEST)
  @ExceptionHandler({ApiException.class})
  public ResponseEntity<?> handleBadRequests(final ApiException e, final WebRequest request) {
    return constructExceptionResponse(e, request, BAD_REQUEST, e.getCode());
  }

  @ResponseStatus(BAD_REQUEST)
  @ExceptionHandler({
    DateTimeParseException.class,
    UnsupportedOperationException.class,
    IllegalArgumentException.class,
    IllegalStateException.class,
    NullPointerException.class
  })
  public ResponseEntity<?> handleBadRequests(final RuntimeException e, final WebRequest request) {
    return constructExceptionResponse(e, request, BAD_REQUEST, ErrorCode.BAD_REQUEST_CODE);
  }

  @ResponseStatus(BAD_REQUEST)
  @ExceptionHandler({BadRequestException.class})
  public ResponseEntity<?> handleBadRequests(
      final BadRequestException e, final WebRequest request) {
    if (e.getExceptionResponse() != null) {
      return new ResponseEntity<>(e.getExceptionResponse(), BAD_REQUEST);
    }
    return constructExceptionResponse(e, request, BAD_REQUEST, e.getCode());
  }

  @ResponseStatus(BAD_REQUEST)
  @ExceptionHandler({AlreadyExistsException.class})
  public ResponseEntity<?> handleBadRequests(
      final AlreadyExistsException e, final WebRequest request) {
    return constructExceptionResponse(e, request, BAD_REQUEST, e.getCode());
  }

  @ResponseStatus(FORBIDDEN)
  @ExceptionHandler({
    ForbiddenException.class,
    CreateForbiddenException.class,
    DeleteForbiddenException.class,
    ListForbiddenException.class,
    RetrieveForbiddenException.class,
    UpdateForbiddenException.class,
    UserBlockedException.class
  })
  public final ResponseEntity<?> handleForbiddenException(
      final ForbiddenException e, final WebRequest request) {
    return constructExceptionResponse(e, request, FORBIDDEN, e.getCode());
  }

  @ResponseStatus(UNPROCESSABLE_ENTITY)
  @ExceptionHandler({JsonParsingException.class, DecodingException.class})
  public final ResponseEntity<?> handleJsonException(final Exception e, final WebRequest request) {
    return constructExceptionResponse(
        e, request, UNPROCESSABLE_ENTITY, ErrorCode.JSON_PARSING_ERROR_CODE);
  }

  @ResponseStatus(NOT_FOUND)
  @ExceptionHandler({NotFoundException.class})
  public ResponseEntity<?> handleNotFoundException(
      final NotFoundException e, final WebRequest request) {
    if (e.getExceptionResponse() != null) {
      return new ResponseEntity<>(e.getExceptionResponse(), NOT_FOUND);
    }
    return constructExceptionResponse(e, request, NOT_FOUND, e.getCode());
  }

  @ResponseStatus(NOT_FOUND)
  @ExceptionHandler({NoSuchElementException.class})
  public ResponseEntity<?> handleNotFoundException(
      final NoSuchElementException e, final WebRequest request) {
    return constructExceptionResponse(e, request, NOT_FOUND, ErrorCode.NOT_FOUND_ERROR_CODE);
  }

  @ResponseStatus(NOT_FOUND)
  @ExceptionHandler({NoResourceFoundException.class})
  public ResponseEntity<?> handleNotFoundException(
      final NoResourceFoundException e, final WebRequest request) {
    return constructExceptionResponse(e, request, NOT_FOUND, ErrorCode.URL_NOT_FOUND);
  }

  @ResponseStatus(NOT_FOUND)
  @ExceptionHandler({HttpRequestMethodNotSupportedException.class})
  public ResponseEntity<?> handleNotFoundException(
      final HttpRequestMethodNotSupportedException e, final WebRequest request) {
    return constructExceptionResponse(e, request, NOT_FOUND, ErrorCode.URL_NOT_FOUND);
  }

  @ResponseStatus(UNAUTHORIZED)
  @ExceptionHandler({
    InvalidCredentialsException.class,
    InvalidTokenException.class,
    UnauthorizedException.class,
  })
  public final ResponseEntity<?> handleUnauthorizedException(
      final UnauthorizedException e, final WebRequest request) {
    if (e.getExceptionResponse() != null) {
      return new ResponseEntity<>(e.getExceptionResponse(), UNAUTHORIZED);
    }
    return constructExceptionResponse(e, request, UNAUTHORIZED, e.getCode());
  }

  @ExceptionHandler({UnauthorizedExceptionYandex.class})
  public final ResponseEntity<?> handleUnauthorizedException2(
      final UnauthorizedException e, final WebRequest request) {
    return constructExceptionResponse(e, request, UNAUTHORIZED, e.getCode());
  }

  @ResponseStatus(SERVICE_UNAVAILABLE)
  @ExceptionHandler(ServiceUnavailableException.class)
  public ResponseEntity<?> handleServiceUnavailableException(
      final ServiceUnavailableException e, final WebRequest request) {
    return constructExceptionResponse(e, request, SERVICE_UNAVAILABLE, e.getCode());
  }

  protected ResponseEntity<Response<?>> constructExceptionResponse(
      final Exception e,
      final WebRequest request,
      final HttpStatus status,
      final ErrorCode errorCode) {
    final String path = request.getDescription(false);
    String language = Util.getNotNull(request.getHeader("Accept-Language"), "uz");

    LOGGER.error("Failed to request [{}] path. Error:", path, e);
    String description =
        messageSource.getMessage(
            errorCode.name(), null, e.getMessage(), Locale.forLanguageTag(language));
    Response<String> response = new Response<>(e.getMessage(), description, null);
    return new ResponseEntity<>(response, status);
  }
}
