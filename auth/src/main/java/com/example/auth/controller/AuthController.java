package com.example.auth.controller;

import com.example.auth.dto.AuthResultDto;
import com.example.auth.dto.LoginRequestDto;
import com.example.auth.dto.RefreshRequestDto;
import com.example.auth.service.AuthService;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Tag(name = "Auth", description = "Operations with authorization")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/api/v1/auth/login")
    public AuthResultDto login(@RequestBody @Valid LoginRequestDto request) {
        return authService.login(request);
    }

    @PostMapping("/api/v1/auth/refresh")
    public AuthResultDto refresh(@RequestBody @Valid RefreshRequestDto request) {
        return authService.refresh(request);
    }
}