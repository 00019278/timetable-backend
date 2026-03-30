package com.sarmich.timetable.model.request;

import com.sarmich.timetable.exception.handler.ErrorCode;
import com.sarmich.timetable.exception.handler.Validator;

public record PasswordResetRequest(String email) {
  public PasswordResetRequest {
    Validator.notNull(ErrorCode.REQUIRED_FIELD_MISSED, email, "Email is required");
  }
}
