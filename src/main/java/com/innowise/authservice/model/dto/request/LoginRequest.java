package com.innowise.authservice.model.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginRequest {

  @NotBlank(message = "Login should not be empty!")
  @Size(min = 1, max = 50, message = "Login must be from 1 to 50 chars!")
  private String login;

  @NotBlank(message = "Password should not be empty!")
  @Size(min = 8, max = 100, message = "Password must be from 8 to 100 chars!")
  private String password;
}
