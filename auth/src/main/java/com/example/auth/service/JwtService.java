package com.example.auth.service;

import com.example.auth.dto.AccessTokenGenerationResultDto;
import com.example.auth.entity.User;
import io.jsonwebtoken.Claims;

public interface JwtService {
    AccessTokenGenerationResultDto generateAccessToken(User user);
    String generateRefreshToken(User user);
    Claims validateToken(String token);
    String extractUserId(String token);
}