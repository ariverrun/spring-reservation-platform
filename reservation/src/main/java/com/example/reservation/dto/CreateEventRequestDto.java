package com.example.reservation.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;

public record CreateEventRequestDto(
    @NotBlank(message = "Name cannot be blank")
    @Size(max = 255, message = "Name must be less than 255 characters")
    String name,

    @Size(max = 2000, message = "Description must be less than 2000 characters")
    String description,

    @NotNull(message = "Start time cannot be null")
    Instant startTime,

    @NotNull(message = "Duration cannot be null")
    @Min(value = 1, message = "Duration must be at least 1 second")
    Long durationSeconds,

    @NotNull(message = "Ticket price cannot be null")
    @Min(value = 0, message = "Ticket price cannot be negative")
    Double ticketPrice,

    @NotNull(message = "Total seats cannot be null")
    @Min(value = 1, message = "Total seats must be at least 1")
    Integer totalSeats
) {
}