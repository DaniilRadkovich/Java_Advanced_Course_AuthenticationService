package com.innowise.authenticationservice.model.dto.response;

import com.innowise.authenticationservice.model.entity.Role;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TokenValidationResponse {
  private boolean active;
  private UUID userId;
  private String login;
  private Role role;
  private String message;
}
