package com.example.reservation.exceptions;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;

import com.example.reservation.dto.RestApiErrorDto;
import com.example.reservation.dto.RestApiValidationErrorDto;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice
public class ErrorHandler {

    @ExceptionHandler({
        NoHandlerFoundException.class, 
        NoHandlerFoundException.class, 
        HttpRequestMethodNotSupportedException.class
    })
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public RestApiErrorDto handleNotFound(Exception ex, HttpServletRequest request) {
        return new RestApiErrorDto("NOT_FOUND", "Resource not found: " + ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public RestApiValidationErrorDto handleInvalidArguments(MethodArgumentNotValidException ex) {
        Map<String, String> violations = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            violations.put(fieldName, errorMessage);
        });

        return new RestApiValidationErrorDto("VALIDATION_FAILED", violations);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public RestApiErrorDto handleMissingQueryParam(
        MissingServletRequestParameterException ex, 
        HttpServletRequest request
    ) {
        return new RestApiErrorDto("MISSING_QUERY_PARAM", "Missing query parameter: " + ex.getParameterName());
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public RestApiErrorDto handleGenericError(Exception ex, HttpServletRequest request) {
        log.error("Internal server error", ex);
        return new RestApiErrorDto("INTERNAL_ERROR", "Internal server error");
    }
}