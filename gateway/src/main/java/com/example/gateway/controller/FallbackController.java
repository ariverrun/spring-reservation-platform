package com.example.gateway.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.gateway.dto.RestApiErrorDto;

import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/fallback")
public class FallbackController {

    @RequestMapping("/auth")
    public Mono<ResponseEntity<RestApiErrorDto>> authFallback() {
        RestApiErrorDto error = new RestApiErrorDto(
            "SERVICE_UNAVAILABLE",
            "Authentication service is currently unavailable."
        );
        return Mono.just(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(error));
    }

    @RequestMapping("/reservation")
    public Mono<ResponseEntity<RestApiErrorDto>> reservationEventFallback() {
        RestApiErrorDto error = new RestApiErrorDto(
            "SERVICE_UNAVAILABLE",
            "Reservation service is currently unavailable."
        );
        return Mono.just(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(error));
    }
}