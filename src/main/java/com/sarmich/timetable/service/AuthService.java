
package com.sarmich.timetable.service;

import com.google.firebase.auth.*;
import com.sarmich.timetable.config.JwtService;
import com.sarmich.timetable.domain.CompanyEntity;
import com.sarmich.timetable.domain.UserEntity;
import com.sarmich.timetable.domain.enums.ProfileRole;
import com.sarmich.timetable.exception.AlreadyExistsException;
import com.sarmich.timetable.exception.InvalidCredentialsException;
import com.sarmich.timetable.exception.UnauthorizedException;
import com.sarmich.timetable.exception.handler.ErrorCode;
import com.sarmich.timetable.model.GetCodeResponse;
import com.sarmich.timetable.model.request.*;
import com.sarmich.timetable.model.response.AuthResponse;
import com.sarmich.timetable.repository.CompanyRepository;
import com.sarmich.timetable.repository.UserRepository;
import java.util.Objects;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
@Slf4j
public class AuthService {

  private final UserRepository userRepository;
  private final VerificationCodeService verificationCodeService;
  private final JwtService jwtService;
  private final CompanyService companyService;
  private final CompanyRepository companyRepository;

  public GetCodeResponse getCode(GetCodeRequest req) {
    log.info("[AUTH] Sending verification to {}", req.email());
    userRepository
            .findByEmail(req.email())
            .ifPresent(
                    u -> {
                      throw new AlreadyExistsException(
                              ErrorCode.ALREADY_EXISTS_ERROR_CODE, "User already exists with this email");
                    });
    return verificationCodeService.generateAndStore(req.email(), 180);
  }

  public AuthResponse verify(VerifyRequest req) {
    log.debug("[AUTH] Verifying code {} email {}", req.code(), req.email());
    verificationCodeService.validate(req.email(), req.code());

    userRepository
            .findByEmail(req.email())
            .ifPresent(
                    u -> {
                      throw new AlreadyExistsException(
                              ErrorCode.ALREADY_EXISTS_ERROR_CODE, "User already exists with this email");
                    });
    UserEntity user = new UserEntity();
    user.setName(req.name());
    user.setSurname(req.surname());
    user.setEmail(req.email());
    user.setPassword(BCrypt.hashpw(req.password(), BCrypt.gensalt()));
    user.setRole(ProfileRole.ROLE_ADMIN);
    UserEntity save = userRepository.save(user);

    CompanyEntity company =
            companyService.create(new CompanyRequest(user.getName(), null, null, null), save.getId());
    String token = jwtService.generateUserToken(user.getId(), company.getId(), "timetable", 0L);
    return new AuthResponse(token);
  }

  public AuthResponse login(LoginRequest dto) {
    Optional<UserEntity> optional = userRepository.findByEmail(dto.email());
    if (optional.isEmpty()) {
      throw new InvalidCredentialsException(
              ErrorCode.INVALID_CREDENTIALS_ERROR_CODE, "email or password incorrect");
    }
    UserEntity user = optional.get();
    if (!BCrypt.checkpw(dto.password(), user.getPassword())) {
      throw new InvalidCredentialsException(
              ErrorCode.INVALID_CREDENTIALS_ERROR_CODE, "email or password incorrect");
    }

    // ✅ ИСПРАВЛЕНО: если компания не найдена — создаём новую
    CompanyEntity company = companyRepository.findByCreatedByAndDeletedFalse(user.getId());
    if (company == null) {
      company = companyService.create(
              new CompanyRequest(user.getName(), null, null, null), user.getId());
    }

    String token = jwtService.generateUserToken(user.getId(), company.getId(), "timetable", 0L);
    return new AuthResponse(token);
  }

  public AuthResponse google(final GoogleLoginRequest request) {
    try {
      FirebaseToken decodedToken = FirebaseAuth.getInstance().verifyIdToken(request.idToken());
      String uid = decodedToken.getUid();
      UserRecord user;
      try {
        user = FirebaseAuth.getInstance().getUser(uid);
      } catch (FirebaseAuthException e) {
        log.error("{}", e.getMessage(), e);
        throw new UnauthorizedException("User not found", e);
      }

      for (UserInfo providerDatum : user.getProviderData()) {
        System.out.println(providerDatum.getProviderId());
      }

      final String email = user.getEmail().toLowerCase();
      final String name = user.getDisplayName();
      final String image = user.getPhotoUrl();

      if (Objects.equals(user.getProviderData()[0].getProviderId(), "google.com")) {
        var entityUser = userRepository.findByEmail(email).orElse(null);
        if (entityUser == null) {
          entityUser = new UserEntity();
        }
        entityUser.setName(name);
        entityUser.setEmail(email);
        entityUser.setPhoto(image);
        entityUser.setRole(ProfileRole.ROLE_USER);
        entityUser = userRepository.save(entityUser);

        // ✅ ИСПРАВЛЕНО: не создаём компанию повторно если уже есть
        CompanyEntity company = companyRepository.findByCreatedByAndDeletedFalse(entityUser.getId());
        if (company == null) {
          company = companyService.create(
                  new CompanyRequest(name, null, null, null), entityUser.getId());
        }

        return new AuthResponse(
                jwtService.generateUserToken(entityUser.getId(), company.getId(), "timetable", 0L));
      } else {
        throw new UnauthorizedException(ErrorCode.UNAUTHORIZED_ERROR_CODE, "Can't find provider");
      }
    } catch (FirebaseAuthException e) {
      throw new RuntimeException(e);
    }
  }
}
