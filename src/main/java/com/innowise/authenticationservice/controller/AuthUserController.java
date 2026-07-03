package com.innowise.authenticationservice.controller;

import com.innowise.authenticationservice.model.dto.request.LoginRequest;
import com.innowise.authenticationservice.model.dto.request.RefreshTokenRequest;
import com.innowise.authenticationservice.model.dto.request.RegisterRequest;
import com.innowise.authenticationservice.model.dto.request.TokenValidationRequest;
import com.innowise.authenticationservice.model.dto.response.PromoteUserResponse;
import com.innowise.authenticationservice.model.dto.response.RegisterResponse;
import com.innowise.authenticationservice.model.dto.response.TokenResponse;
import com.innowise.authenticationservice.model.dto.response.TokenValidationResponse;
import java.util.UUID;
import org.springframework.http.ResponseEntity;

/**
 * The authentication user controller interface. URL prefix: /api/v1/auth
 */
public interface AuthUserController {

  /**
   * Registers a new user in application. URL: /api/v1/auth/register
   *
   * <p>This endpoint also validates the input data and throws exceptions if the data is invalid
   *
   * @param request RegisterRequest which contains the following data: login, name, surname,
   *     birthdate, email and password
   * @return RegisterResponse wrapped in ResponseEntity which contains the following data: userId,
   *     login, role, accessToken, message
   */
  ResponseEntity<RegisterResponse> register(RegisterRequest request);

  /**
   * Authenticates a new user in application. URL: /api/v1/auth/login
   *
   * <p>This endpoint also validates the input data and throws exceptions if the data is invalid
   *
   * @param request LoginRequest which contains the following data: login and password
   * @return TokenResponse wrapped in ResponseEntity which contains the following data: accessToken,
   *     refreshToken
   */
  ResponseEntity<TokenResponse> login(LoginRequest request);

  /**
   * Validates the status of an existing access token. URL: /api/v1/auth/validate
   *
   * <p>This endpoint also validates the input token and throws exceptions if the token is invalid
   *
   * @param request TokenValidationRequest which contains access token
   * @return TokenValidationResponse wrapped in ResponseEntity which contains the following data:
   *     active(a boolean flag indicating whether the token is valid or expired), userId, login,
   *     role, message
   */
  ResponseEntity<TokenValidationResponse> validate(TokenValidationRequest request);

  /**
   * Refreshes expired access token using a valid refresh token. URL: /api/v1/auth/refresh
   *
   * <p>This endpoint also validates the input token and throws exceptions if the token is invalid
   *
   * @param request represents the RefreshTokenRequest containing the refresh token
   * @return TokenResponse wrapped in ResponseEntity which contains the following data: new
   *     accessToken, refreshToken
   */
  ResponseEntity<TokenResponse> refresh(RefreshTokenRequest request);

  /**
   * Promotes a user from the USER role to the ADMIN role. URL: /api/v1/auth/promote/{id}
   *
   * @param id the user id in UUID format
   * @return PromoteUserResponse wrapped in ResponseEntity which contains the following data:
   *     userId, login, role, message
   */
  ResponseEntity<PromoteUserResponse> promote(UUID id);
}
