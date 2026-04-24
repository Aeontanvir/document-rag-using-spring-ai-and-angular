package com.aeon.documentrag.backend.service;

import com.aeon.documentrag.backend.dto.AuthResponse;
import com.aeon.documentrag.backend.dto.LoginRequest;
import com.aeon.documentrag.backend.dto.RegisterRequest;
import com.aeon.documentrag.backend.dto.UserProfileResponse;
import com.aeon.documentrag.backend.entity.AuthTokenEntity;
import com.aeon.documentrag.backend.entity.UserEntity;
import com.aeon.documentrag.backend.exception.ConflictException;
import com.aeon.documentrag.backend.exception.ResourceNotFoundException;
import com.aeon.documentrag.backend.exception.UnauthorizedException;
import com.aeon.documentrag.backend.repository.AuthTokenRepository;
import com.aeon.documentrag.backend.repository.UserRepository;
import com.aeon.documentrag.backend.security.AuthenticatedUser;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private static final Duration SESSION_DURATION = Duration.ofDays(30);

    private final UserRepository userRepository;
    private final AuthTokenRepository authTokenRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String normalizedEmail = normalizeEmail(request.email());
        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new ConflictException("An account already exists for " + normalizedEmail);
        }

        UserEntity user = new UserEntity();
        user.setName(normalizeName(request.name()));
        user.setEmail(normalizedEmail);
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        UserEntity savedUser = userRepository.save(user);
        return createSession(savedUser);
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        String normalizedEmail = normalizeEmail(request.email());
        UserEntity user = userRepository.findByEmail(normalizedEmail)
                .orElseThrow(() -> new UnauthorizedException("Invalid email or password"));

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new UnauthorizedException("Invalid email or password");
        }

        return createSession(user);
    }

    public Optional<AuthenticatedUser> authenticate(String token) {
        if (token == null || token.isBlank()) {
            return Optional.empty();
        }

        return authTokenRepository.findByTokenAndExpiresAtAfter(token, Instant.now())
                .map(AuthTokenEntity::getUser)
                .map(user -> new AuthenticatedUser(user.getId(), user.getEmail(), user.getName()));
    }

    public UserProfileResponse getCurrentUser(String userId) {
        return toUserProfile(getRequiredUser(userId));
    }

    @Transactional
    public void logout(String authorizationHeader) {
        String token = extractBearerToken(authorizationHeader);
        if (token != null) {
            authTokenRepository.deleteByToken(token);
        }
    }

    public UserEntity getRequiredUser(String userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));
    }

    private AuthResponse createSession(UserEntity user) {
        AuthTokenEntity authToken = new AuthTokenEntity();
        authToken.setUser(user);
        authToken.setToken(UUID.randomUUID() + "." + UUID.randomUUID());
        authToken.setExpiresAt(Instant.now().plus(SESSION_DURATION));
        AuthTokenEntity savedToken = authTokenRepository.save(authToken);

        return new AuthResponse(savedToken.getToken(), toUserProfile(user));
    }

    private UserProfileResponse toUserProfile(UserEntity user) {
        return new UserProfileResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getCreatedAt()
        );
    }

    private String normalizeEmail(String email) {
        if (email == null) {
            return "";
        }
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private String normalizeName(String name) {
        if (name == null) {
            return "";
        }
        return name.trim();
    }

    private String extractBearerToken(String authorizationHeader) {
        if (authorizationHeader == null || authorizationHeader.isBlank()) {
            return null;
        }
        if (!authorizationHeader.startsWith("Bearer ")) {
            return null;
        }
        return authorizationHeader.substring(7).trim();
    }
}
