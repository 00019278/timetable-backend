package com.sarmich.timetable.model.common;

public record Response<T>(String error, String errorDescription, T response) {
  public static <T> Response<T> ok(T res) {
    return new Response<>("", "", res);
  }

  public static <T> Response<T> ok() {
    return new Response<>("", "", null);
  }
}
