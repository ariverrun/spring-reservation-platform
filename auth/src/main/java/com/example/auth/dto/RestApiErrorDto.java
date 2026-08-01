package com.example.auth.dto;

public record RestApiErrorDto(
    String error,
    String message
) {
}