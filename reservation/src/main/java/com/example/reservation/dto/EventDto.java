package com.example.reservation.dto;

import java.time.Instant;
import java.util.UUID;

public record EventDto(
    UUID id,
    UUID userId,
    String name,
    String description,
    Instant startTime,
    Long durationSeconds,
    Double ticketPrice,
    Integer totalSeats,
    Boolean isCanceled
) {
}
