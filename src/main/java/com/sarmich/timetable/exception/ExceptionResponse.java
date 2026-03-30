package com.sarmich.timetable.exception;


public record ExceptionResponse(Integer code, String description) {
  public ExceptionResponse {
    if (description == null) description = "";
  }
}
