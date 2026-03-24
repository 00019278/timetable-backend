package com.sarmich.timetable.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SignUpRequest {

    @Size(min = 3)
    private String name;

    @Size(min = 3)
    private String surname;

    @Email(regexp = "[a-z0-9._%+-]+@[a-z0-9.-]+\\.[a-z]{2,3}", flags = Pattern.Flag.CASE_INSENSITIVE)
    private String email;

//    @ValidPassword(message = "Password must be 8 or more characters in length, contain 1 or more uppercase characters, 1 or more lowercase characters, 1 or more special characters.")
    private String password;
}