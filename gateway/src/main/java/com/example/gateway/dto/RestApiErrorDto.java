package com.example.gateway.dto;

public record RestApiErrorDto(
    String error,
    String message
) {
}