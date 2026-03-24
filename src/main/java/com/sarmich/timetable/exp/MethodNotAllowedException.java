package com.sarmich.timetable.exp;

public class MethodNotAllowedException extends RuntimeException {
    public MethodNotAllowedException() {
    }

    public MethodNotAllowedException(String message) {
        super(message);
    }
}
