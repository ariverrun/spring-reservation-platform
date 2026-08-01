package com.example.auth.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.example.auth.dto.AuthResultDto;
import com.example.auth.dto.LoginRequestDto;
import com.example.auth.dto.RefreshRequestDto;
import com.example.auth.service.AuthService;
import com.example.auth.service.JwtService;

@WebMvcTest(value = AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    @MockBean
    private JwtService jwtService;

    @Test
    void shouldLoginSuccessfully() throws Exception {
        LoginRequestDto request = new LoginRequestDto("test@example.com", "password123");
        AuthResultDto expectedResult = new AuthResultDto(
            "access-token",
            "refresh-token",
            UUID.fromString("11111111-1111-1111-1111-111111111111"),
            "test@example.com",
            List.of("ROLE_USER"),
            Instant.parse("2026-01-01T00:00:00Z")
        );
        when(authService.login(any(LoginRequestDto.class))).thenReturn(expectedResult);

        mockMvc.perform(post("/api/v1/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(content().json(objectMapper.writeValueAsString(expectedResult)));

        verify(authService).login(any(LoginRequestDto.class));
    }

    @Test
    void shouldRefreshTokenSuccessfully() throws Exception {
        RefreshRequestDto request = new RefreshRequestDto("refresh-token");
        AuthResultDto expectedResult = new AuthResultDto(
            "new-access-token",
            "new-refresh-token",
            UUID.fromString("11111111-1111-1111-1111-111111111111"),
            "test@example.com",
            List.of("ROLE_USER"),
            Instant.parse("2026-01-01T00:00:00Z")
        );
        when(authService.refresh(any(RefreshRequestDto.class))).thenReturn(expectedResult);

        mockMvc.perform(post("/api/v1/auth/refresh")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(content().json(objectMapper.writeValueAsString(expectedResult)));

        verify(authService).refresh(any(RefreshRequestDto.class));
    }

    @Test
    void shouldReturn400WhenLoginWithInvalidEmail() throws Exception {
        LoginRequestDto invalidRequest = new LoginRequestDto("invalid-email", "password123");

        mockMvc.perform(post("/api/v1/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(invalidRequest)))
            .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturn400WhenLoginWithBlankEmail() throws Exception {
        LoginRequestDto invalidRequest = new LoginRequestDto("", "password123");

        mockMvc.perform(post("/api/v1/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(invalidRequest)))
            .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturn400WhenLoginWithBlankPassword() throws Exception {
        LoginRequestDto invalidRequest = new LoginRequestDto("test@example.com", "");

        mockMvc.perform(post("/api/v1/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(invalidRequest)))
            .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturn400WhenRefreshWithBlankToken() throws Exception {
        RefreshRequestDto invalidRequest = new RefreshRequestDto("");

        mockMvc.perform(post("/api/v1/auth/refresh")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(invalidRequest)))
            .andExpect(status().isBadRequest());
    }

    @ParameterizedTest
    @MethodSource("provideInvalidLoginRequests")
    void shouldReturn400ForInvalidLoginRequests(LoginRequestDto request) throws Exception {
        mockMvc.perform(post("/api/v1/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());
    }

    private static List<LoginRequestDto> provideInvalidLoginRequests() {
        return List.of(
            new LoginRequestDto(null, "password123"),
            new LoginRequestDto("test@example.com", null),
            new LoginRequestDto("", "password123"),
            new LoginRequestDto("test@example.com", ""),
            new LoginRequestDto("invalid-email", "password123"),
            new LoginRequestDto(" ", "password123"),
            new LoginRequestDto("test@example.com", " ")
        );
    }
}