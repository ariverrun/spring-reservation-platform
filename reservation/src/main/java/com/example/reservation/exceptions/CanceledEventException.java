package com.example.reservation.exceptions;

public class CanceledEventException extends RuntimeException {
    public CanceledEventException(String message) {
        super(message);
    }
}