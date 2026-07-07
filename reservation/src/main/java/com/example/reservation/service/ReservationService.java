package com.example.reservation.service;

import java.util.List;
import java.util.UUID;

import com.example.reservation.dto.CreateReservationRequestDto;
import com.example.reservation.dto.ReservationDto;
import com.example.reservation.dto.ReservationWithEventDto;
import com.example.reservation.dto.UserInfo;

public interface ReservationService {
    ReservationDto createReservation(CreateReservationRequestDto request, UserInfo user);
    List<ReservationWithEventDto> getUserReservations(UserInfo user);
    List<ReservationDto> getEventReservations(UUID eventId);
}