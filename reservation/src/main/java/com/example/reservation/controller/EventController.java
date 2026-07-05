package com.example.reservation.controller;

import com.example.reservation.dto.CreateEventRequestDto;
import com.example.reservation.dto.EventCreatedDto;
import com.example.reservation.dto.EventDto;
import com.example.reservation.dto.UserInfo;
import com.example.reservation.service.EventService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@RestController
@RequiredArgsConstructor
public class EventController {

    private final EventService eventService;

    @GetMapping("/api/v1/event")
    public List<EventDto> getAllEvents() {
        return eventService.getAllEvents();
    }

    @GetMapping("/api/v1/event/{id}")
    public EventDto getEventById(@PathVariable UUID id) {
        return eventService.getEventById(id);
    }

    @PostMapping("api/v1/event")
    public EventCreatedDto createEvent(
        @RequestBody @Valid CreateEventRequestDto request,
        @AuthenticationPrincipal UserInfo user
    ) {
        return new EventCreatedDto(
            eventService.createEvent(request, user).id()
        );
    }
}