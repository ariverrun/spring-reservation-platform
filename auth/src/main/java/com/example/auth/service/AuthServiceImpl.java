package com.example.auth.service;

import com.example.auth.dto.AuthResultDto;
import com.example.auth.dto.LoginRequestDto;
import com.example.auth.dto.RefreshRequestDto;
import com.example.auth.entity.User;
import com.example.auth.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthResultDto login(LoginRequestDto request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> {
                    System.out.println("User NOT found!");
                    return new RuntimeException("Invalid credentials");
                });

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new RuntimeException("Invalid credentials");
        }

        return new AuthResultDto(
            jwtService.generateAccessToken(user),
            jwtService.generateRefreshToken(user)
        );
    }

    public AuthResultDto refresh(RefreshRequestDto request) {
        String userId = jwtService.extractUserId(request.refreshToken());
        User user = userRepository.findById(UUID.fromString(userId))
                .orElseThrow(() -> new RuntimeException("User not found"));

        return new AuthResultDto(
            jwtService.generateAccessToken(user),
            jwtService.generateRefreshToken(user)
        );
    }
}