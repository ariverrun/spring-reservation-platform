package com.example.reservation.exceptions;

public class AccessViolationException extends RuntimeException {
    public AccessViolationException(String message) {
        super(message);
    }
}