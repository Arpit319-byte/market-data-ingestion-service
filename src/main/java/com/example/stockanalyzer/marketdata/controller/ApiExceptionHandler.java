package com.example.stockanalyzer.marketdata.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import com.example.stockanalyzer.marketdata.dto.ApiError;
import com.example.stockanalyzer.marketdata.logging.IncidentContext;
import com.example.stockanalyzer.marketdata.exception.AuthenticationException;
import com.example.stockanalyzer.marketdata.exception.ConflictException;
import com.example.stockanalyzer.marketdata.exception.MarketDataException;
import com.example.stockanalyzer.marketdata.exception.NotFoundException;

@RestControllerAdvice
public class ApiExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(ApiExceptionHandler.class);

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ApiError> notFound(NotFoundException exception) {
        return error(HttpStatus.NOT_FOUND, exception.getMessage());
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiError> unknownRoute(NoResourceFoundException exception) {
        return error(HttpStatus.NOT_FOUND, "No endpoint matched this request");
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ApiError> conflict(ConflictException exception) {
        return error(HttpStatus.CONFLICT, exception.getMessage());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiError> dataIntegrity(DataIntegrityViolationException exception) {
        log.warn("Request conflicted with stored data", exception);
        return error(HttpStatus.CONFLICT, "The request conflicts with existing data");
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiError> authentication(AuthenticationException exception) {
        log.warn("Authentication failed: {}", exception.getMessage());
        return error(HttpStatus.UNAUTHORIZED, exception.getMessage());
    }

    @ExceptionHandler(MarketDataException.class)
    public ResponseEntity<ApiError> marketData(MarketDataException exception) {
        if (exception.getCause() == null) {
            return error(HttpStatus.BAD_REQUEST, exception.getMessage());
        }
        log.warn("Market data request failed: {}", exception.getMessage(), exception);
        return error(HttpStatus.BAD_GATEWAY, exception.getMessage());
    }

    @ExceptionHandler({
            MissingServletRequestParameterException.class,
            MethodArgumentTypeMismatchException.class,
            HttpMessageNotReadableException.class
    })
    public ResponseEntity<ApiError> badRequest(Exception exception) {
        return error(HttpStatus.BAD_REQUEST, badRequestMessage(exception));
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiError> methodNotAllowed(HttpRequestMethodNotSupportedException exception) {
        return error(HttpStatus.METHOD_NOT_ALLOWED, "HTTP method is not supported for this endpoint");
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ApiError> unsupportedMedia(HttpMediaTypeNotSupportedException exception) {
        return error(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "Content type is not supported");
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> unexpected(Exception exception) {
        log.error("Unhandled request error", exception);
        return error(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected error");
    }

    private static String badRequestMessage(Exception exception) {
        if (exception instanceof MissingServletRequestParameterException missing) {
            return "Required parameter '" + missing.getParameterName() + "' is missing";
        }
        if (exception instanceof MethodArgumentTypeMismatchException mismatch) {
            return "Parameter '" + mismatch.getName() + "' has an invalid value";
        }
        return "Request body could not be read";
    }

    private static ResponseEntity<ApiError> error(HttpStatus status, String message) {
        String text = message == null || message.isBlank() ? status.getReasonPhrase() : message;
        if (status.is4xxClientError()) {
            log.info("Request failed with {}: {}", status.value(), text);
        }
        return ResponseEntity.status(status)
                .body(new ApiError(status.value(), status.getReasonPhrase(), text, IncidentContext.requestId()));
    }
}
