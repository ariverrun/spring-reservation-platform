package com.example.auth.service;

import com.example.auth.dto.RegisterRequestDto;
import com.example.auth.dto.UserResponseDto;

public interface UserService {
    UserResponseDto register(RegisterRequestDto request);
}