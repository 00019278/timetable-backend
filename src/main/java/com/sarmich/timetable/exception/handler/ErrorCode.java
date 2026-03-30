package com.sarmich.timetable.exception.handler;

import java.util.Arrays;

public enum ErrorCode {
  INVALID_OPERATION_ERROR_CODE(1),
  INVALID_CREDENTIALS_ERROR_CODE(1),
  BAD_REQUEST_CODE(1),
  USER_BLOCKED_ERROR_CODE(1),
  FORBIDDEN_ERROR_CODE(1),
  FILE_REMOVE_ERROR_CODE(1),
  IO_EXCEPTION(1),
  MAX_UPLOAD_SIZE_EXCEEDED(1),
  INVALID_TYPE(1),
  DELETE_FORBIDDEN_ERROR_CODE(1),
  INVALID_TOKEN_ERROR_CODE(1),
  INTERNAL_ERROR_CODE(1),
  LOCKING_CONFLICT_ERROR_CODE(1),
  FILE_UPLOAD_ERROR_CODE(1),
  JSON_PARSING_ERROR_CODE(1),
  REQUIRED_FIELD_MISSED(1),
  SERVICE_UNAVAILABLE_ERROR_CODE(1),
  FILE_DOWNLOAD_ERROR_CODE(1),
  UPDATE_FORBIDDEN_ERROR_CODE(1),
  CREATE_FORBIDDEN_ERROR_CODE(1),
  TIMEOUT_ERROR_CODE(1),
  INVALID_ARGUMENT_ERROR_CODE(1),
  LIST_FORBIDDEN_ERROR_CODE(1),
  RETRIEVE_FORBIDDEN_ERROR_CODE(1),
  UNAUTHORIZED_ERROR_CODE(1),
  API_ERROR_CODE(1000), // == // -------------------------------------------------------
  ALREADY_EXISTS_ERROR_CODE(1100), // ==
  ALREADY_EXISTS_SUB_ERROR_CODE(1100), // ==
  USER_PHONE_EXIST(1100), // == // user exist with this number
  ALREADY_PAID_ORDER(1100), // ==
  ALREADY_CANCELED_OR_DELIVERED_ERROR(1100), // ==
  ALREADY_EXIST_PROMO_CODE(1100), // ==
  ALREADY_COURIER_ASSIGNED_CODE(1100), // ==
  ALREADY_USED_PROMO_CODE(1100), // == // -------------------------------------------------------
  ALREADY_ORDER_CLOSED_OR_DELIVERED(
      1100), // -------------------------------------------------------
  NOT_FOUND_ERROR_CODE(1200),
  URL_NOT_FOUND(1201),
  USER_NOT_FOUND(1202);
  public final int value;

  public static ErrorCode getErrorCode(int code) {
    return Arrays.stream(values())
        .filter(errorCode -> errorCode.value == code)
        .findFirst()
        .orElse(null);
  }

  ErrorCode(int value) {
    this.value = value;
  }
}
