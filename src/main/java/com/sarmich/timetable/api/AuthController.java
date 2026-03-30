package com.sarmich.timetable.api;

import com.sarmich.timetable.model.Response;
import com.sarmich.timetable.model.SmsCache;
import com.sarmich.timetable.model.request.*;
import com.sarmich.timetable.model.response.AuthResponse;
import com.sarmich.timetable.service.AuthService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth/v1")
@AllArgsConstructor
public class AuthController {
    private final AuthService authService;

    // Step 1: Get verification code
    @PostMapping("/code")
    public Response<SmsCache> getCode(@RequestBody GetCodeRequest req) {
        return Response.ok(authService.getCode(req));
    }

    // Step 2: Verify code and register
    @PostMapping("/verify")
    public Response<AuthResponse> verify(@RequestBody VerifyRequest req) {
        return Response.ok(authService.verify(req));
    }

    // Step 3: Login with email/password
    @PostMapping("/login")
    public Response<AuthResponse> login(@RequestBody LoginRequest req) {
        return Response.ok(authService.login(req));
    }

    // Google Sign In
    @PostMapping("/google")
    public Response<AuthResponse> google(@RequestBody GoogleLoginRequest req) {
        return Response.ok(authService.google(req));
    }
}
