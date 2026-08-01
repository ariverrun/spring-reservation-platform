package com.example.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record RefreshRequestDto(
    @NotBlank(message = "Refresh token cannot be blank")
    String refreshToken
) {
}