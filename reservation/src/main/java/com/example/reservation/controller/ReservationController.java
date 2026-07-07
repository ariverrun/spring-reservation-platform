package com.example.reservation.controller;

import com.example.reservation.dto.CreateReservationRequestDto;
import com.example.reservation.dto.ReservationCreatedDto;
import com.example.reservation.dto.ReservationDto;
import com.example.reservation.dto.ReservationWithEventDto;
import com.example.reservation.dto.UserInfo;
import com.example.reservation.service.ReservationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@RestController
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationService reservationService;

    @PostMapping("/api/v1/reserve")
    @ResponseStatus(HttpStatus.CREATED)
    public ReservationCreatedDto createReservation(
        @RequestBody @Valid CreateReservationRequestDto request,
        @AuthenticationPrincipal UserInfo user
    ) {
        return new ReservationCreatedDto(
            reservationService.createReservation(request, user).id()
        );
    }

    @GetMapping("/api/v1/reserve")
    public List<ReservationWithEventDto> getUserReserves(@AuthenticationPrincipal UserInfo user) {
        return reservationService.getUserReservations(user);
    }

    @GetMapping("/api/v1/reserve/{reservationId}")
    public ReservationWithEventDto getReservationById(
        @PathVariable UUID reservationId,
        @AuthenticationPrincipal UserInfo user
    ) {
        return reservationService.getUserReservationById(reservationId, user);
    }
}