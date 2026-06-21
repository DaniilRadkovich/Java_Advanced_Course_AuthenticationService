package com.innowise.authenticationservice.security;

import com.innowise.authenticationservice.exception.TokenLifetimeValidationException;
import com.innowise.authenticationservice.model.entity.AuthUser;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

  private static final long ACCESS_TTL = 60 * 60 * 1000L;
  private static final long REFRESH_TTL = 24 * 60 * 60 * 1000L;

  private final SecretKey secretKey;

  public JwtService(@Value("${jwt.secret-key}") String secret) {
    if (secret == null || secret.isBlank()) {
      throw new IllegalArgumentException("JWT secret must not be empty!");
    }
    byte[] secretBytes = secret.getBytes(StandardCharsets.UTF_8);

    if (secretBytes.length < 32) {
      throw new IllegalArgumentException("JWT secret must be at least 32 bytes long!");
    }
    this.secretKey = Keys.hmacShaKeyFor(secretBytes);
  }

  public String generateAccessToken(AuthUser authUser) {
    Instant now = Instant.now();
    Instant expirationTime = now.plusMillis(ACCESS_TTL);

    return Jwts.builder()
        .subject(authUser.getLogin())
        .claim("id", authUser.getId())
        .claim("role", authUser.getRole())
        .claim("type", "access")
        .issuedAt(Date.from(now))
        .expiration(Date.from(expirationTime))
        .signWith(secretKey)
        .compact();
  }

  public String generateRefreshToken(AuthUser authUser) {
    Instant now = Instant.now();
    Instant expirationTime = now.plusMillis(REFRESH_TTL);

    return Jwts.builder()
        .subject(authUser.getLogin())
        .claim("id", authUser.getId())
        .claim("type", "refresh")
        .issuedAt(Date.from(now))
        .expiration(Date.from(expirationTime))
        .signWith(secretKey)
        .compact();
  }

  public Claims parse(String token) {
    try {
      return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload();
    } catch (ExpiredJwtException e) {
      throw new TokenLifetimeValidationException("The token has expired! ", e);
    } catch (JwtException | IllegalArgumentException e) {
      throw new JwtException("Invalid JWT token data!", e);
    }
  }
}
