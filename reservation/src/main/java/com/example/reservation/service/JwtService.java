package com.example.reservation.service;

import io.jsonwebtoken.Claims;

import java.util.List;

public interface JwtService {
    Claims validateToken(String token) throws Exception;
    String extractUserId(String token) throws Exception;
    List<String> extractRoles(String token) throws Exception;
}