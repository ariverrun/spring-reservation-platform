package com.example.auth.service;

import com.example.auth.dto.AuthResultDto;
import com.example.auth.dto.LoginRequestDto;
import com.example.auth.dto.RefreshRequestDto;
import com.example.auth.entity.RefreshToken;
import com.example.auth.entity.User;
import com.example.auth.exceptions.InvalidCredentialsException;
import com.example.auth.exceptions.InvalidRefreshTokenException;
import com.example.auth.repository.RefreshTokenRepository;
import com.example.auth.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthServiceImpl implements AuthService {

    private static final long REFRESH_TOKEN_DURATION_SECONDS = 7 * 24 * 60 * 60;

    private final UserRepository userRepository;

    private final RefreshTokenRepository refreshTokenRepository;

    private final PasswordEncoder passwordEncoder;

    private final JwtService jwtService;

    @Override
    public AuthResultDto login(LoginRequestDto request) {
        User user = findUserByEmail(request.email());
        validatePassword(request.password(), user.getPassword());

        String refreshToken = createRefreshToken(user);
        var accessTokenResult = jwtService.generateAccessToken(user);

        return buildAuthResult(accessTokenResult.token(), refreshToken, user, accessTokenResult.expiresAt());
    }

    @Override
    public AuthResultDto refresh(RefreshRequestDto request) {
        RefreshToken oldRefreshToken = findValidRefreshToken(request.refreshToken());
        User user = oldRefreshToken.getUser();

        revokeRefreshToken(oldRefreshToken);

        var accessTokenResult = jwtService.generateAccessToken(user);
        String newRefreshToken = createRefreshToken(user);

        return buildAuthResult(accessTokenResult.token(), newRefreshToken, user, accessTokenResult.expiresAt());
    }

    private User findUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new InvalidCredentialsException("Invalid credentials"));
    }

    private void validatePassword(String rawPassword, String encodedPassword) {
        if (!passwordEncoder.matches(rawPassword, encodedPassword)) {
            throw new InvalidCredentialsException("Invalid credentials");
        }
    }

    private RefreshToken findValidRefreshToken(String token) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> new InvalidRefreshTokenException("Invalid refresh token"));

        if (!refreshToken.isValid()) {
            throw new InvalidRefreshTokenException("Refresh token is expired or revoked");
        }

        return refreshToken;
    }

    private String createRefreshToken(User user) {
        String token = jwtService.generateRefreshToken(user);

        RefreshToken refreshTokenEntity = RefreshToken.builder()
                .token(token)
                .user(user)
                .expiresAt(Instant.now().plusSeconds(REFRESH_TOKEN_DURATION_SECONDS))
                .revoked(false)
                .build();

        refreshTokenRepository.save(refreshTokenEntity);
        return token;
    }

    private void revokeRefreshToken(RefreshToken refreshToken) {
        refreshToken.setRevoked(true);
        refreshTokenRepository.save(refreshToken);
    }

    private AuthResultDto buildAuthResult(String accessToken, String refreshToken, User user, Instant expiresAt) {
        List<String> roles = user.getRoles().stream()
                .map(r -> r.getName())
                .toList();

        return new AuthResultDto(
                accessToken,
                refreshToken,
                user.getId(),
                user.getEmail(),
                roles,
                expiresAt
        );
    }
}