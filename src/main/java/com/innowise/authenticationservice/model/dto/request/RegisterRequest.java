package com.innowise.authenticationservice.model.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegisterRequest {

  @NotBlank(message = "Login should not be empty!")
  @Size(min = 1, max = 50, message = "Login must be from 1 to 50 chars!")
  private String login;

  @NotBlank(message = "Name should not be empty!")
  @Size(min = 1, max = 50, message = "Name must be from 1 to 50 chars!")
  private String name;

  @NotBlank(message = "Surname should not be empty!")
  @Size(min = 1, max = 50, message = "Surname must be from 1 to 50 chars!")
  private String surname;

  @Past(message = "Birth date must be in the past!")
  private LocalDate birthDate;

  @NotBlank(message = "Email should not be empty!")
  @Email(message = "Entered invalid email!")
  @Size(max = 100, message = "Email must not be longer than 100 chars!")
  private String email;

  @NotBlank(message = "Password should not be empty!")
  @Size(min = 8, max = 100, message = "Password must be from 8 to 100 chars!")
  private String password;
}
