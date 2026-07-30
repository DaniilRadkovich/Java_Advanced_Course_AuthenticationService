package com.innowise.authservice.repository;

import com.innowise.authservice.model.entity.AuthUser;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuthUserRepository extends JpaRepository<AuthUser, UUID> {

  Optional<AuthUser> findByLogin(String login);

  boolean existsByLogin(String login);
}
