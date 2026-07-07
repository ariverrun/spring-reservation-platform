package com.example.reservation.dto;

import java.util.UUID;

public record ReservationDto(
    UUID id,
    UUID userId,
    ReservationEventDto event,
    Integer seats
) {
}