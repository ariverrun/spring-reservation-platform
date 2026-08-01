package com.example.reservation.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record UpdateReservationRequestDto(
    @NotNull(message = "Seats cannot be null")
    @Min(value = 1, message = "Seats must be at least 1")
    Integer seats,

    @NotNull(message = "IsCanceled cannot be null")
    Boolean isCanceled    
) {
}