package com.example.reservation.service;

import com.example.reservation.dto.CreateEventRequestDto;
import com.example.reservation.dto.EventDto;
import com.example.reservation.dto.UserInfo;

import java.util.List;
import java.util.UUID;

public interface EventService {
    List<EventDto> getAllEvents();
    EventDto getEventById(UUID id);
    EventDto createEvent(CreateEventRequestDto request, UserInfo user);
}