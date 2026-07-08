package com.example.reservation.exceptions;

public class NotEnoughFreeSeatsException extends RuntimeException {
    public NotEnoughFreeSeatsException(String message) {
        super(message);
    }
}