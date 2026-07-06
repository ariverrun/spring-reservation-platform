package com.example.reservation.service;

import com.example.reservation.dto.CreateReservationRequestDto;
import com.example.reservation.dto.ReservationDto;
import com.example.reservation.dto.UserInfo;

public interface ReservationService {
    ReservationDto createReservation(CreateReservationRequestDto request, UserInfo user);
}