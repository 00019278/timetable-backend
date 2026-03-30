package com.sarmich.timetable.model;

public record Response<T>(
    String error,
    String errorDescription,
    T response
) {
    public static <T> Response<T> ok(T res) {
        return new Response<>("", "", res);
    }
}
