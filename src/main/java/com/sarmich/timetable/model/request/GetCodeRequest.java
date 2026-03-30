package com.sarmich.timetable.model.request;

import jakarta.validation.constraints.Email;

public record GetCodeRequest(
    @Email String email
) {
}
