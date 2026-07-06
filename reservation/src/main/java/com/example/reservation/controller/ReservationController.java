package com.example.reservation.controller;

import com.example.reservation.dto.CreateReservationRequestDto;
import com.example.reservation.dto.ReservationCreatedDto;
import com.example.reservation.dto.ReservationDto;
import com.example.reservation.dto.UserInfo;
import com.example.reservation.service.ReservationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/reserve")
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationService reservationService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ReservationCreatedDto createReservation(
        @RequestBody @Valid CreateReservationRequestDto request,
        @AuthenticationPrincipal UserInfo user
    ) {
        return new ReservationCreatedDto(
            reservationService.createReservation(request, user).id()
        );
    }
}