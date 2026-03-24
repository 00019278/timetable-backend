package com.sarmich.timetable.utils;

import com.sarmich.timetable.exp.MethodNotAllowedException;
import com.sarmich.timetable.profile.ProfileRole;
import io.jsonwebtoken.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class JwtUtil {

    private static final long tokenLiveTime = 1000 * 3600 * 24; // 1-day
    private static final String secretKey = "dasda143mazgi";

    public String encode(String email, ProfileRole role) {
        JwtBuilder jwtBuilder = Jwts.builder();
        jwtBuilder.setIssuedAt(new Date());
        jwtBuilder.signWith(SignatureAlgorithm.HS512, secretKey);
        jwtBuilder.claim("email", email);
        jwtBuilder.claim("role", role.name());
        jwtBuilder.setExpiration(new Date(System.currentTimeMillis() + tokenLiveTime));
        jwtBuilder.setIssuer("Kunuz test portali");
        return jwtBuilder.compact();
    }

    public String encodeToUpdateEmail(String email, int pId) {
        JwtBuilder jwtBuilder = Jwts.builder();
        jwtBuilder.setIssuedAt(new Date());
        jwtBuilder.signWith(SignatureAlgorithm.HS512, secretKey);
        jwtBuilder.claim("email", email);
        jwtBuilder.claim("id", pId);
        jwtBuilder.setExpiration(new Date(System.currentTimeMillis() + tokenLiveTime));
        jwtBuilder.setIssuer("Kunuz test portali");
        return jwtBuilder.compact();
    }

    public static String generateJwt(String email, String password, ProfileRole role) {
        JwtBuilder jwtBuilder = Jwts.builder();
        jwtBuilder.setIssuedAt(new Date());
        jwtBuilder.signWith(SignatureAlgorithm.HS512, secretKey);
        jwtBuilder.claim("email", email);
        jwtBuilder.claim("password", password);
        jwtBuilder.claim("role", role.name());
        jwtBuilder.setExpiration(new Date(System.currentTimeMillis() + tokenLiveTime));
        jwtBuilder.setIssuer("Kunuz test portali");
        return jwtBuilder.compact();
    }

    public static JwtDTO decode(String token) {
        JwtParser jwtParser = Jwts.parser();
        jwtParser.setSigningKey(secretKey);
        Jws<Claims> jws = jwtParser.parseClaimsJws(token);
        Claims claims = jws.getBody();
        String email = claims.get("email", String.class);
        String role = claims.get("role", String.class);
        ProfileRole profileRole = ProfileRole.valueOf(role);
        return new JwtDTO(email, profileRole);
    }

    public JwtDTO decodeToUpdateEmail(String token) {
        JwtParser jwtParser = Jwts.parser();
        jwtParser.setSigningKey(secretKey);
        Jws<Claims> jws = jwtParser.parseClaimsJws(token);
        Claims claims = jws.getBody();
        String email = claims.get("email", String.class);
        int pId = claims.get("id", Integer.class);
        return new JwtDTO(email, pId);
    }

    public String decodeEmailVerification(String token) {
        try {
            JwtParser jwtParser = Jwts.parser();
            jwtParser.setSigningKey(secretKey);
            Jws<Claims> jws = jwtParser.parseClaimsJws(token);
            Claims claims = jws.getBody();
            return claims.get("email", String.class);
        } catch (JwtException e) {
            e.printStackTrace();
        }
        throw new MethodNotAllowedException("Jwt exception");
    }

    public JwtDTO getJwtDTO(String authorization) {
        String[] str = authorization.split(" ");
        String jwt = str[1];
        return decode(jwt);
    }

    public JwtDTO getJwtDTO(String authorization, ProfileRole... roleList) {
        String[] str = authorization.split(" ");
        String jwt = str[1];
        JwtDTO jwtDTO = decode(jwt);
        boolean roleFound = false;
        for (ProfileRole role : roleList) {
            if (jwtDTO.getRole() == role) {
                roleFound = true;
                break;
            }
        }
        if (!roleFound) {
            throw new MethodNotAllowedException("Method not allowed");
        }
        return jwtDTO;
    }

    public void checkForRequiredRole(HttpServletRequest request, ProfileRole... roleList) {
        ProfileRole jwtRole = (ProfileRole) request.getAttribute("role");
        boolean roleFound = false;
        for (ProfileRole role : roleList) {
            if (jwtRole == role) {
                roleFound = true;
                break;
            }
        }
        if (!roleFound) {
            throw new MethodNotAllowedException("Method not allowed");
        }
    }
}
