package com.innowise.authenticationservice.controller;

import com.innowise.authenticationservice.model.dto.request.LoginRequest;
import com.innowise.authenticationservice.model.dto.request.RefreshTokenRequest;
import com.innowise.authenticationservice.model.dto.request.RegisterRequest;
import com.innowise.authenticationservice.model.dto.request.TokenValidationRequest;
import com.innowise.authenticationservice.model.dto.response.PromoteUserResponse;
import com.innowise.authenticationservice.model.dto.response.RegisterResponse;
import com.innowise.authenticationservice.model.dto.response.TokenResponse;
import com.innowise.authenticationservice.model.dto.response.TokenValidationResponse;
import com.innowise.authenticationservice.service.AuthService;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * The authentication user controller.
 *
 * <p>REST controller which manages authentication, authorization and role promotion. This
 * controller provides endpoints for user registration, login (authentication), token validation,
 * token refresh and role promotion.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class AuthUserController {

  private final AuthService authService;

  /**
   * Registers a new user in application
   *
   * <p>This endpoint also validates the input data and throws exceptions if the data is invalid
   *
   * @param request RegisterRequest which contains the following data: login, name, surname,
   *     birthdate, email and password
   * @return RegisterResponse wrapped in ResponseEntity which contains the following data: userId,
   *     login, role, accessToken, message
   */
  @PostMapping("/register")
  public ResponseEntity<RegisterResponse> register(@Valid @RequestBody RegisterRequest request) {
    return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(request));
  }

  /**
   * Authenticates a new user in application
   *
   * <p>This endpoint also validates the input data and throws exceptions if the data is invalid
   *
   * @param request LoginRequest which contains the following data: login and password
   * @return TokenResponse wrapped in ResponseEntity which contains the following data: accessToken,
   *     refreshToken
   */
  @PostMapping("/login")
  public ResponseEntity<TokenResponse> login(@Valid @RequestBody LoginRequest request) {
    return ResponseEntity.ok(authService.login(request));
  }

  /**
   * Validates the status of an existing access token
   *
   * <p>This endpoint also validates the input token and throws exceptions if the token is invalid
   *
   * @param request TokenValidationRequest which contains access token
   * @return TokenValidationResponse wrapped in ResponseEntity which contains the following data:
   *     active(a boolean flag indicating whether the token is valid or expired), userId, login,
   *     role, message
   */
  @PostMapping("/validate")
  public ResponseEntity<TokenValidationResponse> validate(
      @Valid @RequestBody TokenValidationRequest request) {
    return ResponseEntity.ok(authService.validate(request));
  }

  /**
   * Refreshes expired access token using a valid refresh token
   *
   * <p>This endpoint also validates the input token and throws exceptions if the token is invalid
   *
   * @param request represents the RefreshTokenRequest containing the refresh token
   * @return TokenResponse wrapped in ResponseEntity which contains the following data: new
   *     accessToken, refreshToken
   */
  @PostMapping("/refresh")
  public ResponseEntity<TokenResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
    return ResponseEntity.ok(authService.refresh(request));
  }

  /**
   * Promotes a user from the USER role to the ADMIN role
   *
   * @param id the user id in UUID format
   * @return PromoteUserResponse wrapped in ResponseEntity which contains the following data:
   *     userId, login, role, message
   */
  @PreAuthorize("hasRole('ADMIN')")
  @PostMapping("/promote/{id}")
  public ResponseEntity<PromoteUserResponse> promote(@PathVariable UUID id) {
    return ResponseEntity.status(HttpStatus.OK).body(authService.promoteToAdmin(id));
  }
}
