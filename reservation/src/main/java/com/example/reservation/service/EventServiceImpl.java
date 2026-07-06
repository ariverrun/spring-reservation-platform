package com.example.reservation.service;

import com.example.reservation.dto.CreateEventRequestDto;
import com.example.reservation.dto.EventDto;
import com.example.reservation.dto.UpdateEventRequestDto;
import com.example.reservation.dto.UserInfo;
import com.example.reservation.entity.Event;
import com.example.reservation.repository.EventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EventServiceImpl implements EventService {

    private final EventRepository eventRepository;

    @Override
    @Transactional(readOnly = true)
    public List<EventDto> getAllEvents() {
        return eventRepository.findAll().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public EventDto getEventById(UUID id) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Event not found with id: " + id));
        return mapToDto(event);
    }

    @Override
    @Transactional
    public EventDto createEvent(CreateEventRequestDto request, UserInfo user) {
        Event event = Event.builder()
                .id(UUID.randomUUID())
                .userId(user.getId())
                .name(request.name())
                .description(request.description())
                .startTime(request.startTime())
                .durationSeconds(request.durationSeconds())
                .ticketPrice(request.ticketPrice())
                .totalSeats(request.totalSeats())
                .build();

        Event savedEvent = eventRepository.save(event);
        return mapToDto(savedEvent);
    }

    @Override
    @Transactional
    public EventDto updateEvent(UUID id, UpdateEventRequestDto request) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Event not found with id: " + id));

        event.setName(request.name());
        event.setDescription(request.description());
        event.setStartTime(request.startTime());
        event.setDurationSeconds(request.durationSeconds());
        event.setTicketPrice(request.ticketPrice());
        event.setTotalSeats(request.totalSeats());
        event.setIsCanceled(request.isCanceled());

        Event updatedEvent = eventRepository.save(event);
        return mapToDto(updatedEvent);
    }

    private EventDto mapToDto(Event event) {
        return new EventDto(
            event.getId(),
            event.getUserId(),
            event.getName(),
            event.getDescription(),
            event.getStartTime(),
            event.getDurationSeconds(),
            event.getTicketPrice(),
            event.getTotalSeats(),
            event.getIsCanceled()
        );
    }
}