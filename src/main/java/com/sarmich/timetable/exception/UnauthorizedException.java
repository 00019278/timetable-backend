package com.sarmich.timetable.exception;

import com.sarmich.timetable.exception.handler.ErrorCode;
import com.sarmich.timetable.exception.handler.ExceptionInterface;
import lombok.Getter;

public class UnauthorizedException extends RuntimeException implements ExceptionInterface {
    private ErrorCode code = ErrorCode.UNAUTHORIZED_ERROR_CODE;
    @Getter
    private ExceptionResponse exceptionResponse;

    public UnauthorizedException() {
    }

    public UnauthorizedException(final ExceptionResponse exceptionResponse) {
        this.exceptionResponse = exceptionResponse;
    }

    public UnauthorizedException(final String message) {
        super(message);
    }

    public UnauthorizedException(final ErrorCode code) {
        super();
        this.code = code;
    }

    public UnauthorizedException(final ErrorCode code, final String message) {
        super(message);
        this.code = code;
    }

    public UnauthorizedException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public UnauthorizedException(final Throwable cause) {
        super(cause);
    }

    @Override
    public ErrorCode getCode() {
        return code;
    }
}
