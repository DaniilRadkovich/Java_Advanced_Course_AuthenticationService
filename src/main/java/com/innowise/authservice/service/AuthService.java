package com.innowise.authservice.service;

import com.innowise.authservice.model.dto.request.LoginRequest;
import com.innowise.authservice.model.dto.request.RefreshTokenRequest;
import com.innowise.authservice.model.dto.request.RegisterRequest;
import com.innowise.authservice.model.dto.request.TokenValidationRequest;
import com.innowise.authservice.model.dto.response.PromoteUserResponse;
import com.innowise.authservice.model.dto.response.RegisterResponse;
import com.innowise.authservice.model.dto.response.TokenResponse;
import com.innowise.authservice.model.dto.response.TokenValidationResponse;
import java.util.UUID;

/**
 * The authentication service interface.
 */
public interface AuthService {

  /**
   * Register a new user
   *
   * @param request RegisterRequest which contains the following data: login, name, surname,
   *     birthdate, email and password
   * @return RegisterResponse which contains the following data: userId, login, role, accessToken,
   *     message
   */
  RegisterResponse register(RegisterRequest request);

  /**
   * Login a user which already exists
   *
   * @param request LoginRequest which contains the following data: login and password
   * @return TokenResponse which contains the following data: accessToken, refreshToken
   */
  TokenResponse login(LoginRequest request);

  /**
   * Refresh the access token
   *
   * @param refreshToken represents the RefreshTokenRequest containing the refresh token
   * @return TokenResponse which contains the following data: new accessToken, refreshToken
   */
  TokenResponse refresh(RefreshTokenRequest refreshToken);

  /**
   * Validate the token
   *
   * @param token TokenValidationRequest which contains the following data: access token
   * @return TokenValidationResponse which contains the following data: active(a boolean flag
   *     indicating whether the token is valid or expired), userId, login, role, message
   */
  TokenValidationResponse validate(TokenValidationRequest token);

  /**
   * Promote a user to admin
   *
   * @param userId the user id in UUID format
   * @return PromoteUserResponse which contains the following data: userId, login, role, message
   */
  PromoteUserResponse promoteToAdmin(UUID userId);
}
