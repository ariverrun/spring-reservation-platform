package com.example.auth.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
import com.example.auth.dto.RegisterRequestDto;
import com.example.auth.dto.UserResponseDto;
import com.example.auth.service.JwtService;
import com.example.auth.service.UserService;

@WebMvcTest(value = UserController.class)
@AutoConfigureMockMvc(addFilters = false)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    @MockBean
    private JwtService jwtService;

    @Test
    void shouldRegisterSuccessfully() throws Exception {
        RegisterRequestDto request = new RegisterRequestDto(
            "test@example.com",
            "John",
            "Doe",
            "password123"
        );
        UserResponseDto expectedResult = new UserResponseDto(UUID.randomUUID());

        when(userService.register(any(RegisterRequestDto.class))).thenReturn(expectedResult);

        mockMvc.perform(post("/api/v1/user")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(content().json(objectMapper.writeValueAsString(expectedResult)));

        verify(userService).register(any(RegisterRequestDto.class));
    }

    @Test
    void shouldReturn400WhenRegisterWithInvalidEmail() throws Exception {
        RegisterRequestDto invalidRequest = new RegisterRequestDto(
            "invalid-email",
            "John",
            "Doe",
            "password123"
        );

        mockMvc.perform(post("/api/v1/user")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(invalidRequest)))
            .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturn400WhenRegisterWithBlankEmail() throws Exception {
        RegisterRequestDto invalidRequest = new RegisterRequestDto(
            "",
            "John",
            "Doe",
            "password123"
        );

        mockMvc.perform(post("/api/v1/user")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(invalidRequest)))
            .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturn400WhenRegisterWithBlankFirstName() throws Exception {
        RegisterRequestDto invalidRequest = new RegisterRequestDto(
            "test@example.com",
            "",
            "Doe",
            "password123"
        );

        mockMvc.perform(post("/api/v1/user")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(invalidRequest)))
            .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturn400WhenRegisterWithFirstNameTooShort() throws Exception {
        RegisterRequestDto invalidRequest = new RegisterRequestDto(
            "test@example.com",
            "J",
            "Doe",
            "password123"
        );

        mockMvc.perform(post("/api/v1/user")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(invalidRequest)))
            .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturn400WhenRegisterWithBlankLastName() throws Exception {
        RegisterRequestDto invalidRequest = new RegisterRequestDto(
            "test@example.com",
            "John",
            "",
            "password123"
        );

        mockMvc.perform(post("/api/v1/user")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(invalidRequest)))
            .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturn400WhenRegisterWithLastNameTooShort() throws Exception {
        RegisterRequestDto invalidRequest = new RegisterRequestDto(
            "test@example.com",
            "John",
            "D",
            "password123"
        );

        mockMvc.perform(post("/api/v1/user")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(invalidRequest)))
            .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturn400WhenRegisterWithBlankPassword() throws Exception {
        RegisterRequestDto invalidRequest = new RegisterRequestDto(
            "test@example.com",
            "John",
            "Doe",
            ""
        );

        mockMvc.perform(post("/api/v1/user")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(invalidRequest)))
            .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturn400WhenRegisterWithPasswordTooShort() throws Exception {
        RegisterRequestDto invalidRequest = new RegisterRequestDto(
            "test@example.com",
            "John",
            "Doe",
            "12345"
        );

        mockMvc.perform(post("/api/v1/user")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(invalidRequest)))
            .andExpect(status().isBadRequest());
    }

    @ParameterizedTest
    @MethodSource("provideInvalidRegisterRequests")
    void shouldReturn400ForInvalidRegisterRequests(RegisterRequestDto request) throws Exception {
        mockMvc.perform(post("/api/v1/user")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());
    }

    private static List<RegisterRequestDto> provideInvalidRegisterRequests() {
        return List.of(
            new RegisterRequestDto(null, "John", "Doe", "password123"),
            new RegisterRequestDto("test@example.com", null, "Doe", "password123"),
            new RegisterRequestDto("test@example.com", "John", null, "password123"),
            new RegisterRequestDto("test@example.com", "John", "Doe", null),
            new RegisterRequestDto("", "John", "Doe", "password123"),
            new RegisterRequestDto("test@example.com", "", "Doe", "password123"),
            new RegisterRequestDto("test@example.com", "John", "", "password123"),
            new RegisterRequestDto("test@example.com", "John", "Doe", ""),
            new RegisterRequestDto("invalid-email", "John", "Doe", "password123"),
            new RegisterRequestDto("test@example.com", "J", "Doe", "password123"),
            new RegisterRequestDto("test@example.com", "John", "D", "password123"),
            new RegisterRequestDto("test@example.com", "John", "Doe", "12345"),
            new RegisterRequestDto("test@example.com", "John", "Doe", "   "),
            new RegisterRequestDto("test@example.com", "   ", "Doe", "password123")
        );
    }
}