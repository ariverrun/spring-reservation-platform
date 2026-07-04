package com.example.auth.dto;

public record AuthResultDto(
    String accessToken,
    String refreshToken
) {
}