package com.sarmich.timetable.model.request;

public record VerifyRequest(
    String name,
    String surname,
    String email,
    Integer code,
    String password
) {
}
