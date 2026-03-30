package com.sarmich.timetable.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.security.Key;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class JwtService {
  @Value("${application.token.signing.key}")
  private String jwtSigningKey;

  public String extractSubject(String token) {
    return extractClaim(token, Claims::getSubject);
  }

  public Claims getAllClaims(String token) {
    return extractAllClaims(token);
  }

  public String generateToken(String sub, String iss, Long expiration) {
    return generateToken(new HashMap<>(), sub, iss, expiration);
  }

  public String generateToken(
      UUID userId, UUID deviceId, UUID sessionId, String iss, Long expiration) {
    return generateToken(
        Map.of("userId", userId, "deviceId", deviceId, "sessionId", sessionId),
        userId.toString(),
        iss,
        expiration);
  }

  // Convenience for our Integer user IDs
  public String generateUserToken(Integer userId, String iss, Long expiration) {
    return generateToken(Map.of("userId", userId.toString()), userId.toString(), iss, expiration);
  }

  public boolean isTokenValid(String token, String sub) {
    final String subject = extractSubject(token);
    return (sub.equals(subject)) && !isTokenExpired(token);
  }

  private <T> T extractClaim(String token, Function<Claims, T> claimsResolvers) {
    final Claims claims = extractAllClaims(token);
    return claimsResolvers.apply(claims);
  }

  private String generateToken(
      Map<String, Object> extraClaims, String sub, String iss, Long expiration) {
    Instant instant = Instant.now();
    final Date currentTime = new Date(instant.toEpochMilli());
    int hour = instant.atZone(ZoneOffset.UTC).getHour();

    if (hour >= 19) {
      instant = instant.plusSeconds(24 * 60 * 60);
    }
    instant = instant.atZone(ZoneOffset.UTC).withHour(23).withMinute(59).withSecond(59).toInstant();

    return Jwts.builder()
        .claims(extraClaims)
        .subject(sub)
        .issuer(iss)
        .issuedAt(currentTime)
        .expiration(new Date(instant.toEpochMilli() + expiration))
        .signWith(getSigningKey())
        .compact();
  }

  private boolean isTokenExpired(String token) {
    return extractExpiration(token).before(new Date(Instant.now().toEpochMilli()));
  }

  private Date extractExpiration(String token) {
    return extractClaim(token, Claims::getExpiration);
  }

  @SuppressWarnings("deprecation")
  private Claims extractAllClaims(String token) {
    return Jwts.parser()
        .setSigningKey(getSigningKey())
        .build()
        .parseSignedClaims(token)
        .getPayload();
  }

  private Key getSigningKey() {
    byte[] keyBytes = Decoders.BASE64.decode(jwtSigningKey);
    return Keys.hmacShaKeyFor(keyBytes);
  }
}
