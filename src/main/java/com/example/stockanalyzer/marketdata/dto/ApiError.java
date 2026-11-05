package com.example.stockanalyzer.marketdata.dto;

public record ApiError(int status, String error, String message, String requestId) {
}
