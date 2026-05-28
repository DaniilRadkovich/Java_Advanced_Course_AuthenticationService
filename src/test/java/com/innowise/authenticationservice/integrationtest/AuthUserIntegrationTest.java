package com.innowise.authenticationservice.integrationtest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.innowise.authenticationservice.model.dto.request.LoginRequest;
import com.innowise.authenticationservice.model.dto.request.RefreshTokenRequest;
import com.innowise.authenticationservice.model.dto.request.RegisterRequest;
import com.innowise.authenticationservice.model.dto.request.TokenValidationRequest;
import com.innowise.authenticationservice.model.dto.response.PromoteUserResponse;
import com.innowise.authenticationservice.model.dto.response.RegisterResponse;
import com.innowise.authenticationservice.model.dto.response.TokenResponse;
import com.innowise.authenticationservice.model.dto.response.TokenValidationResponse;
import com.innowise.authenticationservice.model.entity.AuthUser;
import com.innowise.authenticationservice.model.entity.Role;
import com.innowise.authenticationservice.repository.AuthUserRepository;
import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.springframework.security.test.context.support.WithMockUser;

@Testcontainers
@SpringBootTest(webEnvironment = WebEnvironment.MOCK)
@AutoConfigureMockMvc
class AuthUserIntegrationTest {

  @Autowired private MockMvc mockMvc;

  @Autowired private ObjectMapper objectMapper;

  @Autowired private AuthUserRepository authUserRepository;

  @Autowired private PasswordEncoder passwordEncoder;

  @BeforeEach
  void setUp() {
    authUserRepository.deleteAll();
  }

  @Test
  void should_success_register() throws Exception {
    RegisterRequest request =
        RegisterRequest.builder()
            .login("JR")
            .name("Jack")
            .surname("Richer")
            .birthDate(LocalDate.of(1980, 1, 1))
            .email("jackricher@mail.com")
            .password("password")
            .build();

    String response =
        mockMvc
            .perform(
                post("/api/v1/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andReturn()
            .getResponse()
            .getContentAsString();

    RegisterResponse responseDto = objectMapper.readValue(response, RegisterResponse.class);

    assertThat(responseDto.getUserId()).isNotNull();
    assertThat(responseDto.getLogin()).isEqualTo("JR");
    assertThat(responseDto.getRole()).isEqualTo(Role.USER);
    assertThat(responseDto.getAccessToken()).isNotBlank();

    AuthUser savedUser = authUserRepository.findByLogin("JR").orElseThrow();

    assertThat(savedUser.getLogin()).isEqualTo("JR");
    assertThat(savedUser.getRole()).isEqualTo(Role.USER);
    assertThat(passwordEncoder.matches("password", savedUser.getPassword())).isTrue();
  }

  @Test
  void should_success_login() throws Exception {

    AuthUser authUser = new AuthUser();
    authUser.setLogin("JR");
    authUser.setPassword(passwordEncoder.encode("password"));
    authUser.setRole(Role.USER);

    authUserRepository.save(authUser);

    LoginRequest request = new LoginRequest("JR", "password");

    String response =
        mockMvc
            .perform(
                post("/api/v1/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

    TokenResponse responseDto = objectMapper.readValue(response, TokenResponse.class);

    assertThat(responseDto.getAccessToken()).isNotBlank();
    assertThat(responseDto.getRefreshToken()).isNotBlank();
  }

  @Test
  void should_success_validateToken() throws Exception {

    RegisterRequest registerRequest =
        RegisterRequest.builder()
            .login("JR")
            .name("Jack")
            .surname("Richer")
            .birthDate(LocalDate.of(1980, 1, 1))
            .email("jackricher@mail.com")
            .password("password")
            .build();

    String registerResponse =
        mockMvc
            .perform(
                post("/api/v1/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(registerRequest)))
            .andExpect(status().isCreated())
            .andReturn()
            .getResponse()
            .getContentAsString();

    RegisterResponse registerDto = objectMapper.readValue(registerResponse, RegisterResponse.class);

    TokenValidationRequest request = new TokenValidationRequest(registerDto.getAccessToken());

    String response =
        mockMvc
            .perform(
                post("/api/v1/auth/validate")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

    TokenValidationResponse responseDto =
        objectMapper.readValue(response, TokenValidationResponse.class);

    assertThat(responseDto.isActive()).isTrue();
    assertThat(responseDto.getLogin()).isEqualTo("JR");
    assertThat(responseDto.getRole()).isEqualTo(Role.USER);
  }

  @Test
  void should_success_refreshToken() throws Exception {

    AuthUser authUser = new AuthUser();
    authUser.setLogin("JR");
    authUser.setPassword(passwordEncoder.encode("password"));
    authUser.setRole(Role.USER);

    authUserRepository.save(authUser);

    LoginRequest loginRequest = new LoginRequest("JR", "password");

    String loginResponse =
        mockMvc
            .perform(
                post("/api/v1/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(loginRequest)))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

    TokenResponse loginDto = objectMapper.readValue(loginResponse, TokenResponse.class);

    RefreshTokenRequest request = new RefreshTokenRequest(loginDto.getRefreshToken());

    String response =
        mockMvc
            .perform(
                post("/api/v1/auth/refresh")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

    TokenResponse responseDto = objectMapper.readValue(response, TokenResponse.class);

    assertThat(responseDto.getAccessToken()).isNotBlank();
    assertThat(responseDto.getRefreshToken()).isNotBlank();
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void should_success_promoteUser() throws Exception {

    AuthUser authUser = new AuthUser();
    authUser.setLogin("JR");
    authUser.setPassword(passwordEncoder.encode("password"));
    authUser.setRole(Role.USER);

    AuthUser savedUser = authUserRepository.save(authUser);

    UUID id = savedUser.getId();

    String response =
        mockMvc
            .perform(post("/api/v1/auth/promote/{id}", id))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

    PromoteUserResponse responseDto = objectMapper.readValue(response, PromoteUserResponse.class);

    assertThat(responseDto.getRole()).isEqualTo(Role.ADMIN);

    AuthUser updatedUser = authUserRepository.findById(id).orElseThrow();

    assertThat(updatedUser.getRole()).isEqualTo(Role.ADMIN);
  }
}
