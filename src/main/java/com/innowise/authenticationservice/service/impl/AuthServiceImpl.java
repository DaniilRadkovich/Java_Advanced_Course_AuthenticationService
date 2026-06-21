package com.innowise.authenticationservice.service.impl;

import com.innowise.authenticationservice.exception.InvalidTokenException;
import com.innowise.authenticationservice.exception.UserNotFoundException;
import com.innowise.authenticationservice.exception.UserRegisterException;
import com.innowise.authenticationservice.exception.WrongPasswordException;
import com.innowise.authenticationservice.model.dto.request.LoginRequest;
import com.innowise.authenticationservice.model.dto.request.RefreshTokenRequest;
import com.innowise.authenticationservice.model.dto.request.RegisterRequest;
import com.innowise.authenticationservice.model.dto.request.TokenValidationRequest;
import com.innowise.authenticationservice.model.dto.request.UserCreateRequest;
import com.innowise.authenticationservice.model.dto.response.PromoteUserResponse;
import com.innowise.authenticationservice.model.dto.response.RegisterResponse;
import com.innowise.authenticationservice.model.dto.response.TokenResponse;
import com.innowise.authenticationservice.model.dto.response.TokenValidationResponse;
import com.innowise.authenticationservice.model.dto.response.UserCreateResponse;
import com.innowise.authenticationservice.model.entity.AuthUser;
import com.innowise.authenticationservice.model.entity.Role;
import com.innowise.authenticationservice.repository.AuthUserRepository;
import com.innowise.authenticationservice.service.AuthService;
import com.innowise.authenticationservice.security.JwtService;
import com.innowise.authenticationservice.security.UserServiceClient;
import io.jsonwebtoken.Claims;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

  private static final String USER_ALREADY_EXISTS_MESSAGE = "User already exists! Login: ";
  private static final String USER_NOT_FOUND_MESSAGE = "User not found! Login or ID: ";

  private final AuthUserRepository authUserRepository;
  private final JwtService jwtService;
  private final PasswordEncoder passwordEncoder;
  private final UserServiceClient userServiceClient;

  @Override
  public RegisterResponse register(RegisterRequest request) {
    if (authUserRepository.existsByLogin(request.getLogin())) {
      throw new UserRegisterException(USER_ALREADY_EXISTS_MESSAGE + request.getLogin());
    }

    UserCreateResponse createdUser = null;

    try {
      createdUser = userServiceClient.createUser(
              UserCreateRequest.builder()
                  .name(request.getName())
                  .surname(request.getSurname())
                  .birthDate(request.getBirthDate())
                  .email(request.getEmail())
                  .build());

      AuthUser authUser = new AuthUser();
      authUser.setId(createdUser.getId());
      authUser.setLogin(request.getLogin());
      authUser.setPassword(passwordEncoder.encode(request.getPassword()));

      AuthUser savedUser = authUserRepository.save(authUser);
      String accessToken = jwtService.generateAccessToken(savedUser);

      return new RegisterResponse(
          savedUser.getId(),
          savedUser.getLogin(),
          savedUser.getRole(),
          accessToken,
          "User registered successfully!");
    } catch (Exception e) {
      log.error("Registration failed before compensation", e);
      if (createdUser != null) {
        try {
          userServiceClient.deleteUser(createdUser.getId());
        } catch (Exception ex) {
          log.error("Compensation failed", ex);
        }
      }
      log.error("Registration failed", e);
      throw new UserRegisterException("Registration failed");
    }
  }

  @Override
  public TokenResponse login(LoginRequest request) {
    AuthUser authUser =
        authUserRepository
            .findByLogin(request.getLogin())
            .orElseThrow(
                () -> new UserNotFoundException(USER_NOT_FOUND_MESSAGE + request.getLogin()));
    if (!passwordEncoder.matches(request.getPassword(), authUser.getPassword())) {
      throw new WrongPasswordException("Invalid password!");
    }

    String accessToken = jwtService.generateAccessToken(authUser);
    String refreshToken = jwtService.generateRefreshToken(authUser);
    return new TokenResponse(accessToken, refreshToken, "User logged in successfully!");
  }

  @Override
  public TokenResponse refresh(RefreshTokenRequest request) {
    Claims claims = jwtService.parse(request.getRefreshToken());

    String tokenType = claims.get("type", String.class);

    if (!"refresh".equals(tokenType)) {
      throw new InvalidTokenException("Invalid refresh token");
    }

    String login = claims.getSubject();
    AuthUser authUser =
        authUserRepository
            .findByLogin(login)
            .orElseThrow(() -> new UserNotFoundException(USER_NOT_FOUND_MESSAGE + login));

    String newAccessToken = jwtService.generateAccessToken(authUser);
    String newRefreshToken = jwtService.generateRefreshToken(authUser);
    return new TokenResponse(newAccessToken, newRefreshToken, "Token refreshed successfully!");
  }

  @Override
  public TokenValidationResponse validate(TokenValidationRequest request) {
    try {
      Claims claims = jwtService.parse(request.getToken());
      UUID userId = UUID.fromString(claims.get("id", String.class));
      String login = claims.getSubject();
      Role role = Role.valueOf(claims.get("role", String.class));

      return new TokenValidationResponse(
          true, userId, login, role, "Token is valid! Access granted!");
    } catch (Exception e) {
      throw new BadCredentialsException("Token is invalid! Access denied! Required token refresh!");
    }
  }

  @Override
  @Transactional
  public PromoteUserResponse promoteToAdmin(UUID userId) {
    AuthUser authUser =
        authUserRepository
            .findById(userId)
            .orElseThrow(() -> new UserNotFoundException(USER_NOT_FOUND_MESSAGE + userId));

    authUser.setRole(Role.ADMIN);
    return new PromoteUserResponse(
        authUser.getId(),
        authUser.getLogin(),
        authUser.getRole(),
        "User promoted to admin successfully!");
  }

  private void rollbackAuthUser(UUID userId) {
    try {
      authUserRepository.deleteById(userId);
      log.info("User with ID: {} has been rolled back!", userId);
    } catch (Exception e) {
      throw new UserRegisterException("Failed to rollback user with ID: " + userId);
    }
  }
}
