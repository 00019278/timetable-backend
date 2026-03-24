package com.sarmich.timetable.auth;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("auth")
@AllArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("sign-up")
    public void signUp(@RequestBody @Valid SignUpRequest dto) {
        authService.signUp(dto);
    }

    @PostMapping("sign-in")
    public ResponseEntity<?> signIn(@RequestBody LoginRequest dto) {
        return authService.signIn(dto);
    }
}
