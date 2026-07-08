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

@Service
@RequiredArgsConstructor
@Transactional
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Override
    @Transactional(readOnly = false)
    public AuthResultDto login(LoginRequestDto request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> {
                    return new InvalidCredentialsException("Invalid credentials");
                });

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new InvalidCredentialsException("Invalid credentials");
        }

        String refreshToken = jwtService.generateRefreshToken(user);

        RefreshToken refreshTokenEntity = RefreshToken.builder()
                .token(refreshToken)
                .user(user)
                .expiresAt(Instant.now().plusSeconds(7 * 24 * 60 * 60))
                .revoked(false)
                .build();

        refreshTokenRepository.save(refreshTokenEntity);

        return new AuthResultDto(
            jwtService.generateAccessToken(user),
            refreshToken
        );
    }

    @Override
    @Transactional
    public AuthResultDto refresh(RefreshRequestDto request) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(request.refreshToken())
                .orElseThrow(() -> new InvalidRefreshTokenException("Invalid refresh token"));

        if (!refreshToken.isValid()) {
            throw new InvalidRefreshTokenException("Refresh token is expired or revoked");
        }

        User user = refreshToken.getUser();

        refreshToken.setRevoked(true);
        refreshTokenRepository.save(refreshToken);

        String newAccessToken = jwtService.generateAccessToken(user);
        String newRefreshToken = jwtService.generateRefreshToken(user);

        RefreshToken newRefreshTokenEntity = RefreshToken.builder()
                .token(newRefreshToken)
                .user(user)
                .expiresAt(Instant.now().plusSeconds(7 * 24 * 60 * 60))
                .revoked(false)
                .build();
        refreshTokenRepository.save(newRefreshTokenEntity);

        return new AuthResultDto(newAccessToken, newRefreshToken);
    }
}