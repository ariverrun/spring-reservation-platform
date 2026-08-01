package com.example.auth.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record AuthResultDto(
    String accessToken,
    String refreshToken,
    UUID userId,
    String email,
    List<String> roles,
    Instant expiresAt
) {
}