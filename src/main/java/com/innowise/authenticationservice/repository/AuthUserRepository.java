package com.innowise.authenticationservice.repository;

import com.innowise.authenticationservice.model.entity.AuthUser;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuthUserRepository extends JpaRepository<AuthUser, UUID> {

  Optional<AuthUser> findByLogin(String login);

  boolean existsByLogin(String login);
}
