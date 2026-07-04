package com.example.reservation.service;

import com.example.reservation.dto.EventDto;
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

    private EventDto mapToDto(Event event) {
        return new EventDto(
            event.getId(),
            event.getUserId(),
            event.getName(),
            event.getDescription(),
            event.getStartTime(),
            event.getDurationSeconds(),
            event.getTicketPrice(),
            event.getTotalSeats()
        );
    }
}