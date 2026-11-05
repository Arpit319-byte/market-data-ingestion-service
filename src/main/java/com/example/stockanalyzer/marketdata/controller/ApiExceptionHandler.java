package com.example.stockanalyzer.marketdata.controller;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.example.stockanalyzer.marketdata.exception.AuthenticationException;
import com.example.stockanalyzer.marketdata.exception.ConflictException;
import com.example.stockanalyzer.marketdata.exception.MarketDataException;
import com.example.stockanalyzer.marketdata.exception.NotFoundException;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<Map<String, String>> notFound(NotFoundException exception) {
        return error(HttpStatus.NOT_FOUND, exception.getMessage());
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<Map<String, String>> conflict(ConflictException exception) {
        return error(HttpStatus.CONFLICT, exception.getMessage());
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<Map<String, String>> authentication(AuthenticationException exception) {
        return error(HttpStatus.BAD_GATEWAY, exception.getMessage());
    }

    @ExceptionHandler(MarketDataException.class)
    public ResponseEntity<Map<String, String>> marketData(MarketDataException exception) {
        return error(HttpStatus.BAD_REQUEST, exception.getMessage());
    }

    private static ResponseEntity<Map<String, String>> error(HttpStatus status, String message) {
        return ResponseEntity.status(status).body(Map.of("error", message == null ? status.getReasonPhrase() : message));
    }
}
