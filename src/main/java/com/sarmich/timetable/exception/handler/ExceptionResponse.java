package com.sarmich.timetable.exception.handler;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ExceptionResponse(String error, String errorDescription) {

  public ExceptionResponse(final Exception exception, final String description) {
    this(exception.getMessage(), description);
  }
}
