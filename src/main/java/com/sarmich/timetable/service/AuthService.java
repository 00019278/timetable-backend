package com.sarmich.timetable.service;

import com.google.firebase.auth.*;
import com.sarmich.timetable.config.JwtService;
import com.sarmich.timetable.domain.UserEntity;
import com.sarmich.timetable.domain.enums.ProfileRole;
import com.sarmich.timetable.exception.AlreadyExistsException;
import com.sarmich.timetable.exception.InvalidCredentialsException;
import com.sarmich.timetable.exception.UnauthorizedException;
import com.sarmich.timetable.model.SmsCache;
import com.sarmich.timetable.model.request.GetCodeRequest;
import com.sarmich.timetable.model.request.GoogleLoginRequest;
import com.sarmich.timetable.model.request.LoginRequest;
import com.sarmich.timetable.model.request.VerifyRequest;
import com.sarmich.timetable.model.response.AuthResponse;
import com.sarmich.timetable.repository.UserRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.Optional;

@Service
@AllArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final VerificationCodeService verificationCodeService;
    private final JwtService jwtService;

    public SmsCache getCode(GetCodeRequest req) {
        log.info("[AUTH] Sending verification  to {}", req.email());
        userRepository
            .findByEmail(req.email())
            .ifPresent(u -> {
                throw new AlreadyExistsException("User already exists with this email");
            });
        return verificationCodeService.generateAndStore(req.email());
    }

    public AuthResponse verify(VerifyRequest req) {
        log.debug("[AUTH] Verifying code {} email {}", req.code(), req.email());
        verificationCodeService.validate(req.email(), req.code());

        userRepository
            .findByEmail(req.email())
            .ifPresent(u -> {
                throw new AlreadyExistsException("User already exists with this email");
            });
        UserEntity user = new UserEntity();
        user.setName(req.name());
        user.setSurname(req.surname());
        user.setEmail(req.email());
        user.setPassword(BCrypt.hashpw(req.password(), BCrypt.gensalt()));
        user.setRole(ProfileRole.ROLE_ADMIN);
        userRepository.save(user);

        String token = jwtService.generateUserToken(user.getId(), "timetable", 0L);
        return new AuthResponse(token);
    }

    public AuthResponse login(LoginRequest dto) {
        Optional<UserEntity> optional = userRepository.findByEmail(dto.email());
        if (optional.isEmpty()) {
            throw new InvalidCredentialsException("email or password incorrect");
        }
        UserEntity user = optional.get();
        if (!BCrypt.checkpw(dto.password(), user.getPassword())) {
            throw new InvalidCredentialsException("email or password incorrect");
        }
        String token = jwtService.generateUserToken(user.getId(), "timetable", 0L);
        return new AuthResponse(token);
    }

    public AuthResponse google(final GoogleLoginRequest request) {
        try {
            FirebaseToken decodedToken =
                FirebaseAuth.getInstance().verifyIdToken(request.idToken());
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
            //      boolean verifyEmail = user.isEmailVerified();
            final String image = user.getPhotoUrl();
            //      String phone = user.getPhoneNumber();
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
                return new AuthResponse(
                    jwtService.generateUserToken(
                        entityUser.getId(),
                        "timetable",
                        0L));
            } else {
                throw new UnauthorizedException("Can't find provider");
            }
        } catch (FirebaseAuthException e) {
            throw new RuntimeException(e);
        }
    }
}
