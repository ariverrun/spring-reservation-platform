package com.example.reservation.exceptions;

public class CanceledReservationException extends RuntimeException {
    public CanceledReservationException(String message) {
        super(message);
    }
}