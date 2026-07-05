package com.example.auth.service;

import com.example.auth.entity.User;
import io.jsonwebtoken.Claims;

public interface JwtService {
    String generateAccessToken(User user);
    String generateRefreshToken(User user);
    Claims validateToken(String token);
    String extractUserId(String token);
}