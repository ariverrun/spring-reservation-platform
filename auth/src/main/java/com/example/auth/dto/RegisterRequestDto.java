package com.example.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequestDto(
    @NotBlank(message = "Email cannot be blank")
    @Email(message = "Email should be valid")
    String email,

    @NotBlank(message = "First name cannot be blank")
    @Size(min = 2, max = 100, message = "First name must be between 2 and 100 characters")
    String firstName,

    @NotBlank(message = "Last name cannot be blank")
    @Size(min = 2, max = 100, message = "Last name must be between 2 and 100 characters")
    String lastName,

    @NotBlank(message = "Password cannot be blank")
    @Size(min = 6, message = "Password must be at least 6 characters")
    String password
) {
}