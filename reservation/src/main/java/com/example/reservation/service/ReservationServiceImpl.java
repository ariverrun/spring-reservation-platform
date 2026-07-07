package com.example.reservation.service;

import com.example.reservation.dto.CreateReservationRequestDto;
import com.example.reservation.dto.ReservationDto;
import com.example.reservation.dto.ReservationEventDto;
import com.example.reservation.dto.ReservationWithEventDto;
import com.example.reservation.dto.UserInfo;
import com.example.reservation.entity.Event;
import com.example.reservation.entity.Reservation;
import com.example.reservation.repository.EventRepository;
import com.example.reservation.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReservationServiceImpl implements ReservationService {

    private final ReservationRepository reservationRepository;
    private final EventRepository eventRepository;

    @Override
    @Transactional
    public ReservationDto createReservation(CreateReservationRequestDto request, UserInfo user) {
        Event event = eventRepository.findByIdWithPessimisticLock(request.eventId())
                .orElseThrow(() -> new RuntimeException("Event not found"));

        if (Boolean.TRUE.equals(event.getIsCanceled())) {
            throw new RuntimeException("Event is canceled");
        }

        long bookedSeats = reservationRepository.countByEventId(event.getId());
        long availableSeats = event.getTotalSeats() - bookedSeats;

        if (availableSeats < request.seats()) {
            throw new RuntimeException("Not enough available seats. Available: " + availableSeats);
        }

        Reservation reservation = Reservation.builder()
                .id(UUID.randomUUID())
                .userId(user.getId())
                .event(event)
                .seats(request.seats())
                .build();

        Reservation saved = reservationRepository.save(reservation);

        return mapToDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReservationWithEventDto> getUserReservations(UserInfo user) {
        List<Reservation> reservations = reservationRepository.findAllByUserIdWithEvent(user.getId());
        
        return reservations.stream()
            .map(this::mapToDtoWithEvent)
            .collect(Collectors.toList())
        ;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReservationDto> getEventReservations(UUID eventId) {
        Event event = eventRepository.findById(eventId)
            .orElseThrow(() -> {
                return new RuntimeException("Event not found with id: " + eventId);
            });
        
        return event.getReservations().stream()
            .map(this::mapToDto)
            .collect(Collectors.toList());
    }

    private ReservationDto mapToDto(Reservation reservation) {
        return new ReservationDto(
            reservation.getId(),
            reservation.getUserId(),
            reservation.getEvent().getId(),
            reservation.getSeats()
        );
    }

    private ReservationWithEventDto mapToDtoWithEvent(Reservation reservation) {
        Event event = reservation.getEvent();
        return new ReservationWithEventDto(
            reservation.getId(),
            reservation.getUserId(),
            new ReservationEventDto(
                event.getId(),
                event.getName(),
                event.getDescription(),
                event.getStartTime(),
                event.getDurationSeconds(),
                event.getTicketPrice(),
                event.getIsCanceled()
            ),
            reservation.getSeats()
        );
    }
}