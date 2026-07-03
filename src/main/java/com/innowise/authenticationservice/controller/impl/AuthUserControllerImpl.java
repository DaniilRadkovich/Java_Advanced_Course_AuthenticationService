package com.innowise.authenticationservice.controller.impl;

import com.innowise.authenticationservice.controller.AuthUserController;
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

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class AuthUserControllerImpl implements AuthUserController {

  private final AuthService authService;

  @PostMapping("/register")
  public ResponseEntity<RegisterResponse> register(@Valid @RequestBody RegisterRequest request) {
    return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(request));
  }

  @PostMapping("/login")
  public ResponseEntity<TokenResponse> login(@Valid @RequestBody LoginRequest request) {
    return ResponseEntity.ok(authService.login(request));
  }

  @PostMapping("/validate")
  public ResponseEntity<TokenValidationResponse> validate(
      @Valid @RequestBody TokenValidationRequest request) {
    return ResponseEntity.ok(authService.validate(request));
  }

  @PostMapping("/refresh")
  public ResponseEntity<TokenResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
    return ResponseEntity.ok(authService.refresh(request));
  }

  @PreAuthorize("hasRole('ADMIN')")
  @PostMapping("/promote/{id}")
  public ResponseEntity<PromoteUserResponse> promote(@PathVariable UUID id) {
    return ResponseEntity.status(HttpStatus.OK).body(authService.promoteToAdmin(id));
  }
}
