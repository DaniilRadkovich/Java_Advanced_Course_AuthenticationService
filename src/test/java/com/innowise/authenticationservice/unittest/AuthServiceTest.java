package com.innowise.authenticationservice.unittest;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
import com.innowise.authenticationservice.security.JwtService;
import com.innowise.authenticationservice.security.UserServiceClient;
import com.innowise.authenticationservice.service.impl.AuthServiceImpl;
import io.jsonwebtoken.Claims;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

  @Mock private AuthUserRepository authUserRepository;

  @Mock private JwtService jwtService;

  @Mock private PasswordEncoder passwordEncoder;

  @Mock private UserServiceClient userServiceClient;

  @Mock private Claims claims;

  @InjectMocks private AuthServiceImpl authService;

  private AuthUser authUser;

  @BeforeEach
  void setUp() {
    authUser = new AuthUser();
    authUser.setId(UUID.randomUUID());
    authUser.setLogin("DonDon");
    authUser.setPassword("encodedPassword");
    authUser.setRole(Role.USER);
  }

  @Test
  void should_success_register() {
    RegisterRequest request =
        RegisterRequest.builder()
            .login("DonDon")
            .name("Don")
            .surname("Jackson")
            .birthDate(LocalDate.of(1990, 2, 2))
            .email("don@email.com")
            .password("password")
            .build();

    UserCreateResponse userCreateResponse = new UserCreateResponse();
    userCreateResponse.setId(authUser.getId());

    when(authUserRepository.existsByLogin("DonDon")).thenReturn(false);
    when(passwordEncoder.encode("password")).thenReturn("encodedPassword");
    when(userServiceClient.createUser(any(UserCreateRequest.class))).thenReturn(userCreateResponse);
    when(authUserRepository.save(any(AuthUser.class))).thenReturn(authUser);
    when(jwtService.generateAccessToken(authUser)).thenReturn("accessToken");

    RegisterResponse response = authService.register(request);

    assertThat(response).isNotNull();
    assertThat(response.getLogin()).isEqualTo("DonDon");
    assertThat(response.getAccessToken()).isEqualTo("accessToken");
    assertThat(response.getRole()).isEqualTo(Role.USER);
    verify(authUserRepository).save(any(AuthUser.class));
  }

  @Test
  void should_throwException_register() {
    RegisterRequest request =
        RegisterRequest.builder()
            .login("DonDon")
            .name("Don")
            .surname("Jackson")
            .birthDate(LocalDate.of(1990, 2, 2))
            .email("don@email.com")
            .password("password")
            .build();

    when(authUserRepository.existsByLogin("DonDon")).thenReturn(true);
    assertThatThrownBy(() -> authService.register(request))
        .isInstanceOf(UserRegisterException.class)
        .hasMessageContaining("User already exists");
    verify(authUserRepository, never()).save(any());
  }

  @Test
  void should_throwException_whenDbFalls_register() {
    RegisterRequest request =
        RegisterRequest.builder()
            .login("DonDon")
            .name("Don")
            .surname("Jackson")
            .birthDate(LocalDate.of(1990, 2, 2))
            .email("don@email.com")
            .password("password")
            .build();

    UserCreateResponse userCreateResponse = new UserCreateResponse();
    userCreateResponse.setId(authUser.getId());

    when(authUserRepository.existsByLogin(request.getLogin())).thenReturn(false);
    when(userServiceClient.createUser(any(UserCreateRequest.class))).thenReturn(userCreateResponse);
    when(passwordEncoder.encode(request.getPassword())).thenReturn("encodedPassword");
    when(authUserRepository.save(any(AuthUser.class))).thenThrow(new RuntimeException("DB falls"));

    assertThatThrownBy(() -> authService.register(request))
        .isInstanceOf(UserRegisterException.class)
        .hasMessage("Registration failed!");

    verify(userServiceClient).deleteUser(authUser.getId());
    verify(authUserRepository, never()).delete(any(AuthUser.class));
  }

  @Test
  void should_throwExceptionAndDeleteUser_whenCompensationFails_register() {
    RegisterRequest request =
        RegisterRequest.builder()
            .login("DonDon")
            .name("Don")
            .surname("Jackson")
            .birthDate(LocalDate.of(1990, 2, 2))
            .email("don@email.com")
            .password("password")
            .build();

    UserCreateResponse userCreateResponse = new UserCreateResponse();
    userCreateResponse.setId(authUser.getId());

    when(authUserRepository.existsByLogin(request.getLogin())).thenReturn(false);
    when(userServiceClient.createUser(any(UserCreateRequest.class))).thenReturn(userCreateResponse);
    when(passwordEncoder.encode(request.getPassword())).thenReturn("encodedPassword");
    when(authUserRepository.save(any(AuthUser.class))).thenThrow(new RuntimeException("DB falls"));

    doThrow(new RuntimeException("Network falls"))
        .when(userServiceClient)
        .deleteUser(authUser.getId());

    assertThatThrownBy(() -> authService.register(request))
        .isInstanceOf(UserRegisterException.class)
        .hasMessage("Registration failed!");

    verify(userServiceClient).deleteUser(authUser.getId());
  }

  @Test
  void should_rollbackAuthUserAndExternalUser_whenTokenGenerationFails() {
    RegisterRequest request =
        RegisterRequest.builder()
            .login("DonDon")
            .name("Don")
            .surname("Jackson")
            .birthDate(LocalDate.of(1990, 2, 2))
            .email("don@email.com")
            .password("password")
            .build();

    UserCreateResponse userCreateResponse = new UserCreateResponse();
    userCreateResponse.setId(authUser.getId());

    when(authUserRepository.existsByLogin(request.getLogin())).thenReturn(false);
    when(userServiceClient.createUser(any(UserCreateRequest.class))).thenReturn(userCreateResponse);
    when(passwordEncoder.encode(request.getPassword())).thenReturn("encodedPassword");
    when(authUserRepository.save(any(AuthUser.class))).thenReturn(authUser);
    when(jwtService.generateAccessToken(any(AuthUser.class)))
        .thenThrow(new RuntimeException("JWT generation failed"));

    assertThatThrownBy(() -> authService.register(request))
        .isInstanceOf(UserRegisterException.class)
        .hasMessage("Registration failed!");

    verify(userServiceClient).deleteUser(authUser.getId());
    verify(authUserRepository).delete(authUser);
    verify(authUserRepository).flush();
  }

  @Test
  void should_returnToken_login() {
    LoginRequest request = new LoginRequest("DonDon", "password");

    when(authUserRepository.findByLogin("DonDon")).thenReturn(Optional.of(authUser));
    when(passwordEncoder.matches("password", "encodedPassword")).thenReturn(true);
    when(jwtService.generateAccessToken(authUser)).thenReturn("accessToken");
    when(jwtService.generateRefreshToken(authUser)).thenReturn("refreshToken");

    TokenResponse response = authService.login(request);

    assertThat(response).isNotNull();
    assertThat(response.getAccessToken()).isEqualTo("accessToken");
    assertThat(response.getRefreshToken()).isEqualTo("refreshToken");
    assertThat(response.getMessage()).isEqualTo("User logged in successfully!");
  }

  @Test
  void should_throwException_login() {
    LoginRequest request = new LoginRequest("DonDon", "wrongPassword");

    when(authUserRepository.findByLogin("DonDon")).thenReturn(Optional.of(authUser));
    when(passwordEncoder.matches("wrongPassword", "encodedPassword")).thenReturn(false);
    assertThatThrownBy(() -> authService.login(request))
        .isInstanceOf(WrongPasswordException.class)
        .hasMessageContaining("Invalid password");
  }

  @Test
  void should_returnRefreshedToken_refresh() {
    RefreshTokenRequest request = new RefreshTokenRequest("refreshToken");

    when(jwtService.parse("refreshToken")).thenReturn(claims);
    when(claims.get("type", String.class)).thenReturn("refresh");
    when(claims.getSubject()).thenReturn("DonDon");
    when(authUserRepository.findByLogin("DonDon")).thenReturn(Optional.of(authUser));
    when(jwtService.generateAccessToken(authUser)).thenReturn("new-access");
    when(jwtService.generateRefreshToken(authUser)).thenReturn("new-refresh");

    TokenResponse response = authService.refresh(request);

    assertThat(response.getAccessToken()).isEqualTo("new-access");
    assertThat(response.getRefreshToken()).isEqualTo("new-refresh");
    assertThat(response.getMessage()).isEqualTo("Token refreshed successfully!");
  }

  @Test
  void should_throwException_refresh() {
    RefreshTokenRequest request = new RefreshTokenRequest("invalid-token");

    when(jwtService.parse("invalid-token")).thenReturn(claims);
    when(claims.get("type", String.class)).thenReturn("access");
    assertThatThrownBy(() -> authService.refresh(request))
        .isInstanceOf(InvalidTokenException.class)
        .hasMessageContaining("Invalid refresh token");
  }

  @Test
  void should_returnValidResponse_validate() {
    UUID userId = UUID.randomUUID();

    TokenValidationRequest request = new TokenValidationRequest("valid-token");

    when(jwtService.parse("valid-token")).thenReturn(claims);
    when(claims.get("id", String.class)).thenReturn(userId.toString());
    when(claims.getSubject()).thenReturn("DonDon");
    when(claims.get("role", String.class)).thenReturn("USER");

    TokenValidationResponse response = authService.validate(request);

    assertThat(response.isActive()).isTrue();
    assertThat(response.getUserId()).isEqualTo(userId);
    assertThat(response.getLogin()).isEqualTo("DonDon");
    assertThat(response.getRole()).isEqualTo(Role.USER);
  }

  @Test
  void should_returnInvalidResponse_validate() {
    TokenValidationRequest request = new TokenValidationRequest("invalid-token");

    when(jwtService.parse("invalid-token")).thenThrow(new RuntimeException());
    assertThatThrownBy(() -> authService.validate(request))
        .isInstanceOf(BadCredentialsException.class)
        .hasMessageContaining("Token is invalid! Access denied! Required token refresh!");
  }

  @Test
  void should_success_promoteToAdmin() {
    UUID userId = authUser.getId();

    when(authUserRepository.findById(userId)).thenReturn(Optional.of(authUser));

    PromoteUserResponse response = authService.promoteToAdmin(userId);

    assertThat(response).isNotNull();
    assertThat(response.getRole()).isEqualTo(Role.ADMIN);
    assertThat(authUser.getRole()).isEqualTo(Role.ADMIN);
  }

  @Test
  void should_throwException_promoteToAdmin() {
    UUID userId = UUID.randomUUID();

    when(authUserRepository.findById(userId)).thenReturn(Optional.empty());
    assertThatThrownBy(() -> authService.promoteToAdmin(userId))
        .isInstanceOf(UserNotFoundException.class)
        .hasMessageContaining("User not found");
  }
}
