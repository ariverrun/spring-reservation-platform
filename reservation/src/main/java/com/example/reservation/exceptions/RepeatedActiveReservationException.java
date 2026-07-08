package com.example.reservation.exceptions;

public class RepeatedActiveReservationException extends RuntimeException {
    public RepeatedActiveReservationException(String message) {
        super(message);
    }
}