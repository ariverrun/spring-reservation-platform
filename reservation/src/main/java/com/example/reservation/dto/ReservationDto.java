package com.example.reservation.dto;

import java.util.UUID;

public record ReservationDto(
    UUID id,
    UUID userId,
    UUID eventId,
    Integer seats,
    Boolean isCanceled
) {
}