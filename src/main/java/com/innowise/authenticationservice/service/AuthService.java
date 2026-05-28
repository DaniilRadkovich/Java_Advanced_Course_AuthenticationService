package com.innowise.authenticationservice.service;

import com.innowise.authenticationservice.model.dto.request.LoginRequest;
import com.innowise.authenticationservice.model.dto.request.RefreshTokenRequest;
import com.innowise.authenticationservice.model.dto.request.RegisterRequest;
import com.innowise.authenticationservice.model.dto.request.TokenValidationRequest;
import com.innowise.authenticationservice.model.dto.response.PromoteUserResponse;
import com.innowise.authenticationservice.model.dto.response.RegisterResponse;
import com.innowise.authenticationservice.model.dto.response.TokenResponse;
import com.innowise.authenticationservice.model.dto.response.TokenValidationResponse;
import java.util.UUID;

public interface AuthService {

  RegisterResponse register(RegisterRequest request);

  TokenResponse login(LoginRequest request);

  TokenResponse refresh(RefreshTokenRequest refreshToken);

  TokenValidationResponse validate(TokenValidationRequest token);

  PromoteUserResponse promoteToAdmin(UUID userId);
}
