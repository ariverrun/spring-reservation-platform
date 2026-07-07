package com.example.reservation.dto;

import java.time.Instant;
import java.util.UUID;

public record ReservationEventDto(
    UUID id,
    String name,
    String description,
    Instant startTime,
    Long durationSeconds,
    Double ticketPrice,
    Boolean isCanceled
) {
}