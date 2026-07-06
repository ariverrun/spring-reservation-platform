package com.example.reservation.service;

import com.example.reservation.dto.CreateReservationRequestDto;
import com.example.reservation.dto.ReservationDto;
import com.example.reservation.dto.UserInfo;
import com.example.reservation.entity.Event;
import com.example.reservation.entity.Reservation;
import com.example.reservation.repository.EventRepository;
import com.example.reservation.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

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
                .eventId(event.getId())
                .seats(request.seats())
                .build();

        Reservation saved = reservationRepository.save(reservation);

        return new ReservationDto(
            saved.getId(),
            saved.getUserId(),
            saved.getEventId(),
            saved.getSeats()
        );
    }
}