package com.example.reservation.service;

import com.example.reservation.dto.EventDto;

import java.util.List;
import java.util.UUID;

public interface EventService {
    List<EventDto> getAllEvents();
    EventDto getEventById(UUID id);
}