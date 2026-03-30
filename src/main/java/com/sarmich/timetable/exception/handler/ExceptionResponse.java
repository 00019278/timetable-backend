package com.sarmich.timetable.exception.handler;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.sarmich.timetable.exception.ApiException;

import java.util.NoSuchElementException;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ExceptionResponse(
    String error,
    String errorDescription) {

    public ExceptionResponse(
        final Exception exception,
        final String description) {
        this(
            exception.getMessage(),
            description
        );
    }

    private static int findErrorCode(final Exception e) {
        if (e instanceof ApiException) {
            return ErrorCode.API_ERROR_CODE.value;
        }

        if (e instanceof ExceptionInterface) {
            return ((ExceptionInterface) e).getCode().value;
        }

        if (e instanceof NoSuchElementException) {
            return ErrorCode.NOT_FOUND_ERROR_CODE.value;
        }

        if (e instanceof NullPointerException) {
            return ErrorCode.NULL_POINTER_ERROR_CODE.value;
        }

        if (e instanceof UnsupportedOperationException) {
            return ErrorCode.UNSUPPORTED_OPERATION_ERROR_CODE.value;
        }

        if (e instanceof IllegalArgumentException) {
            return ErrorCode.INVALID_ARGUMENT_ERROR_CODE.value;
        }
        return ErrorCode.INTERNAL_ERROR_CODE.value;
    }
}
