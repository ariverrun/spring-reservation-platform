package com.example.auth.service;

import com.example.auth.dto.AuthResultDto;
import com.example.auth.dto.LoginRequestDto;
import com.example.auth.dto.RefreshRequestDto;

public interface AuthService {
    AuthResultDto login(LoginRequestDto request);

    AuthResultDto refresh(RefreshRequestDto request);
}