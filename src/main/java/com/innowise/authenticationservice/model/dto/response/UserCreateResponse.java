package com.innowise.authenticationservice.model.dto.response;

import java.time.LocalDate;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserCreateResponse {
  private UUID id;
  private String name;
  private String surname;
  private LocalDate birthDate;
  private String email;
  private boolean active;
}
