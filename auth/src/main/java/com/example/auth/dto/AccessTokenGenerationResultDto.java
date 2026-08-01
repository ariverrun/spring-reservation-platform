package com.example.auth.dto;

import java.time.Instant;

public record AccessTokenGenerationResultDto(
    String token,
    Instant expiresAt
) {
}
