package com.sarmich.timetable.model.request;

import com.sarmich.timetable.exception.handler.ErrorCode;
import com.sarmich.timetable.exception.handler.Validator;

public record PasswordResetVerificationRequest(Integer code, String newPassword, String email) {
  public PasswordResetVerificationRequest {
    Validator.isTrue(
        ErrorCode.REQUIRED_FIELD_MISSED,
        code != null && newPassword != null && email != null,
        "Code  and password is required");
  }
}
