package com.sarmich.timetable.model.request;

import com.sarmich.timetable.annotation.ValidPassword;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;


public record SignUpRequest(

        @Size(min = 3)
        String name,

        @Size(min = 3)
        String surname,

        @Email(regexp = "[a-z0-9._%+-]+@[a-z0-9.-]+\\.[a-z]{2,3}", flags = Pattern.Flag.CASE_INSENSITIVE)
        String email,

        @ValidPassword(message = "Password must be 8 or more characters in length, contain 1 or more uppercase characters, 1 or more lowercase characters, 1 or more special characters.")
        String password
) {
}