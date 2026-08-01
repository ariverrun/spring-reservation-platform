package com.example.reservation.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record CreateReservationRequestDto(
    @NotNull(message = "Event ID cannot be null")
    UUID eventId,

    @NotNull(message = "Seats cannot be null")
    @Min(value = 1, message = "Seats must be at least 1")
    Integer seats
) {
}