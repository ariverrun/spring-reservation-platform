package com.example.reservation.service;

import com.example.reservation.dto.CreateReservationRequestDto;
import com.example.reservation.dto.ReservationDto;
import com.example.reservation.dto.ReservationEventDto;
import com.example.reservation.dto.ReservationWithEventDto;
import com.example.reservation.dto.UpdateReservationRequestDto;
import com.example.reservation.dto.UserInfo;
import com.example.reservation.entity.Event;
import com.example.reservation.entity.Reservation;
import com.example.reservation.exceptions.AccessViolationException;
import com.example.reservation.exceptions.CanceledEventException;
import com.example.reservation.exceptions.CanceledReservationException;
import com.example.reservation.exceptions.EntityNotFoundException;
import com.example.reservation.exceptions.NotEnoughFreeSeatsException;
import com.example.reservation.exceptions.RepeatedActiveReservationException;
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
        Event event = getEventWithLock(request.eventId());
        validateEventNotCanceled(event);
        validateNoActiveReservation(user.getId(), event.getId());
        validateAvailableSeats(event, request.seats());

        Reservation reservation = createReservationEntity(event, user.getId(), request.seats());
        Reservation saved = reservationRepository.save(reservation);

        return mapToDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReservationWithEventDto> getUserReservations(UserInfo user) {
        List<Reservation> reservations = reservationRepository.findAllByUserIdWithEvent(user.getId());
        return reservations.stream()
                .map(this::mapToDtoWithEvent)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReservationDto> getEventReservations(UUID eventId) {
        Event event = getEventById(eventId);
        return event.getReservations().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ReservationWithEventDto getUserReservationById(UUID id, UserInfo user) {
        Reservation reservation = getReservationById(id);
        validateUserOwnsReservation(reservation, user);
        return mapToDtoWithEvent(reservation);
    }

    @Override
    @Transactional
    public ReservationDto updateReservation(UUID id, UpdateReservationRequestDto request, UserInfo user) {
        Reservation reservation = getReservationById(id);
        validateUserOwnsReservation(reservation, user);
        validateReservationNotCanceled(reservation);

        Event event = getEventWithLock(reservation.getEvent().getId());
        validateEventNotCanceled(event);
        validateAvailableSeatsForUpdate(event, reservation, request.seats());

        updateReservationEntity(reservation, request);
        Reservation updated = reservationRepository.save(reservation);

        return mapToDto(updated);
    }

    private Event getEventWithLock(UUID eventId) {
        return eventRepository.findByIdWithPessimisticLock(eventId)
                .orElseThrow(() -> new EntityNotFoundException("Event not found"));
    }

    private Event getEventById(UUID eventId) {
        return eventRepository.findById(eventId)
                .orElseThrow(() -> new EntityNotFoundException("Event not found with id: " + eventId));
    }

    private Reservation getReservationById(UUID id) {
        return reservationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Reservation not found with id: " + id));
    }

    private void validateEventNotCanceled(Event event) {
        if (Boolean.TRUE.equals(event.getIsCanceled())) {
            throw new CanceledEventException("Cannot perform operation on canceled event");
        }
    }

    private void validateNoActiveReservation(UUID userId, UUID eventId) {
        boolean hasActiveReservation = reservationRepository.existsByUserIdAndEventIdAndIsNotCanceled(userId, eventId);
        if (hasActiveReservation) {
            throw new RepeatedActiveReservationException("You already have an active reservation for this event");
        }
    }

    private void validateAvailableSeats(Event event, int requestedSeats) {
        long bookedSeats = reservationRepository.countSeatsByEventId(event.getId());
        long availableSeats = event.getTotalSeats() - bookedSeats;

        if (availableSeats < requestedSeats) {
            throw new NotEnoughFreeSeatsException("Not enough available seats. Available: " + availableSeats);
        }
    }

    private void validateAvailableSeatsForUpdate(Event event, Reservation reservation, int requestedSeats) {
        long bookedSeats = reservationRepository.countSeatsByEventId(event.getId());
        long availableSeats = event.getTotalSeats() - bookedSeats + reservation.getSeats();

        if (availableSeats < requestedSeats) {
            throw new NotEnoughFreeSeatsException("Not enough available seats. Available: " + availableSeats);
        }
    }

    private void validateUserOwnsReservation(Reservation reservation, UserInfo user) {
        if (!reservation.getUserId().equals(user.getId())) {
            throw new AccessViolationException("Access denied");
        }
    }

    private void validateReservationNotCanceled(Reservation reservation) {
        if (Boolean.TRUE.equals(reservation.getIsCanceled())) {
            throw new CanceledReservationException("Cannot update canceled reservation");
        }
    }

    private Reservation createReservationEntity(Event event, UUID userId, int seats) {
        return Reservation.builder()
                .userId(userId)
                .event(event)
                .seats(seats)
                .build();
    }

    private void updateReservationEntity(Reservation reservation, UpdateReservationRequestDto request) {
        reservation.setSeats(request.seats());
        reservation.setIsCanceled(request.isCanceled());
    }

    private ReservationDto mapToDto(Reservation reservation) {
        return new ReservationDto(
                reservation.getId(),
                reservation.getUserId(),
                reservation.getEvent().getId(),
                reservation.getSeats(),
                reservation.getIsCanceled()
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
                reservation.getSeats(),
                reservation.getIsCanceled()
        );
    }
}