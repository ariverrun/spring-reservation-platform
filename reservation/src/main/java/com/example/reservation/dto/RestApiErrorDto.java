package com.example.reservation.dto;

public record RestApiErrorDto(
    String error,
    String message
) {
}