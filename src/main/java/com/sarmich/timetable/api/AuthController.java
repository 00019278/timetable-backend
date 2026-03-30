package com.sarmich.timetable.api;

import com.sarmich.timetable.model.GetCodeResponse;
import com.sarmich.timetable.model.common.Response;
import com.sarmich.timetable.model.request.*;
import com.sarmich.timetable.model.response.AuthResponse;
import com.sarmich.timetable.service.AuthService;
import com.sarmich.timetable.service.PasswordResetService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth/v1")
@AllArgsConstructor
public class AuthController {
  private final AuthService authService;
  private final PasswordResetService passwordResetService;

  // Step 1: Get verification code
  @PostMapping("/code")
  public Response<GetCodeResponse> getCode(@RequestBody GetCodeRequest req) {
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

  @PostMapping("/reset")
  public Response<Void> requestPasswordReset(@RequestBody PasswordResetRequest request) {
    passwordResetService.createPasswordResetTokenForUser(request.email());
    return Response.ok();
  }

  @PostMapping("/reset/verify")
  public Response<Void> verifyPasswordReset(@RequestBody PasswordResetVerificationRequest request) {
    passwordResetService.validatePasswordResetToken(request);
    return Response.ok();
  }
}
