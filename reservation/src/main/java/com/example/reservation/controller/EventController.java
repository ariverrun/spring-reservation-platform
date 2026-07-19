package com.example.reservation.controller;

import com.example.reservation.dto.CreateEventRequestDto;
import com.example.reservation.dto.EventCreatedDto;
import com.example.reservation.dto.EventDto;
import com.example.reservation.dto.ReservationDto;
import com.example.reservation.dto.UpdateEventRequestDto;
import com.example.reservation.dto.UserInfo;
import com.example.reservation.service.EventService;
import com.example.reservation.service.ReservationService;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@Tag(name = "Events", description = "Operations with events")
public class EventController {

    private final EventService eventService;

    private final ReservationService reservationService;

    @GetMapping("/api/v1/event")
    public List<EventDto> getAllEvents() {
        return eventService.getAllEvents();
    }

    @GetMapping("/api/v1/event/{id}")
    public EventDto getEventById(@PathVariable UUID id) {
        return eventService.getEventById(id);
    }

    @PostMapping("/api/v1/event")
    @ResponseStatus(HttpStatus.CREATED)
    public EventCreatedDto createEvent(
        @RequestBody @Valid CreateEventRequestDto request,
        @AuthenticationPrincipal UserInfo user
    ) {
        return new EventCreatedDto(
            eventService.createEvent(request, user).id()
        );
    }

    @PutMapping("/api/v1/event/{id}")
    public EventDto updateEvent(
        @PathVariable UUID id,
        @RequestBody @Valid UpdateEventRequestDto request
    ) {
        return eventService.updateEvent(id, request);
    }

    @GetMapping("/api/v1/event/{id}/reserve")
    public List<ReservationDto> getEventReserves(@PathVariable UUID id) {
        return reservationService.getEventReservations(id);
    }
}