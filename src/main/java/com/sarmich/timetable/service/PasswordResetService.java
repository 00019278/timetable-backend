package com.sarmich.timetable.service;

import com.sarmich.timetable.domain.UserEntity;
import com.sarmich.timetable.exception.NotFoundException;
import com.sarmich.timetable.exception.handler.ErrorCode;
import com.sarmich.timetable.model.request.PasswordResetVerificationRequest;
import com.sarmich.timetable.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class PasswordResetService {
  private final VerificationCodeService verificationCodeService;
  private final UserRepository userRepository;

  public void createPasswordResetTokenForUser(String email) {
    userRepository
        .findByEmail(email)
        .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND_ERROR_CODE, "User not found"));
    verificationCodeService.generateAndStore(email, 300);
  }

  public void validatePasswordResetToken(PasswordResetVerificationRequest request) {
    UserEntity user =
        userRepository
            .findByEmail(request.email())
            .orElseThrow(
                () -> new NotFoundException(ErrorCode.NOT_FOUND_ERROR_CODE, "User not found"));
    verificationCodeService.validate(request.email(), request.code());
    user.setPassword(BCrypt.hashpw(request.newPassword(), BCrypt.gensalt()));
    userRepository.save(user);
  }
}
