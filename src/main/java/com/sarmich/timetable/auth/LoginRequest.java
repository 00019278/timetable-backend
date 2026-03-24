package com.sarmich.timetable.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;


public record LoginRequest(

        @Email
        String email,

        @Size(min = 6)
        String password) {
}