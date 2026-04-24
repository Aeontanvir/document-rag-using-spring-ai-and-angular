package com.aeon.documentrag.backend.repository;

import com.aeon.documentrag.backend.entity.AuthTokenEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.Optional;

public interface AuthTokenRepository extends JpaRepository<AuthTokenEntity, Long> {

    Optional<AuthTokenEntity> findByTokenAndExpiresAtAfter(String token, Instant instant);

    void deleteByToken(String token);
}
