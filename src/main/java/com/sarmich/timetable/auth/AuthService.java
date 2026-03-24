package com.sarmich.timetable.auth;

import com.sarmich.timetable.exp.exception.BadRequestException;
import com.sarmich.timetable.mapper.AuthMapper;
import com.sarmich.timetable.profile.ProfileRepository;
import com.sarmich.timetable.profile.ProfileRole;
import com.sarmich.timetable.utils.JwtUtil;
import com.sarmich.timetable.utils.MD5Util;
import com.sarmich.timetable.utils.ProfileEntity;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class AuthService {

    private final ProfileRepository profileRepository;

    public void signUp(SignUpRequest request) {
        ProfileEntity entity = AuthMapper.INSTANCE.toEntity(request);
        entity.setPassword(MD5Util.convertToMD5(request.password()));
        entity.setRole(ProfileRole.ROLE_ADMIN);
        profileRepository.save(entity);
    }

    public ResponseEntity<?> signIn(LoginRequest dto) {
        if ("admin@gmail.com".equals(dto.email()) && "admin".equals(dto.password())) {
            return ResponseEntity.ok(new AuthResponse(
                    JwtUtil.generateJwt(dto.email(), dto.password(), ProfileRole.ROLE_ADMIN)
            ));
        }
        throw new BadRequestException("email or password incorrect");
    }
}
