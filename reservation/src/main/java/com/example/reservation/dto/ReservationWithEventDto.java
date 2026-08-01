package com.example.reservation.dto;

import java.util.UUID;

public record ReservationWithEventDto(
    UUID id,
    UUID userId,
    ReservationEventDto event,
    Integer seats,
    Boolean isCanceled
) {
}