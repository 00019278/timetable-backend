package com.sarmich.timetable.model.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record SignUpRequest(
    @Size(min = 3) String name,
    @Size(min = 3) String surname,
    @Email(
        regexp = "[a-z0-9._%+-]+@[a-z0-9.-]+\\.[a-z]{2,3}",
        flags = Pattern.Flag.CASE_INSENSITIVE)
    String email,
    @Size(min = 6)
    String password) {
}
