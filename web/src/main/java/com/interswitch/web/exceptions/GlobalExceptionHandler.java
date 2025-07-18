package com.interswitch.web.exceptions;


import com.interswitch.shared.exceptions.ApiException;
import com.interswitch.web.response.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ErrorResponse> handleApiExceptions(ApiException ex, HttpServletRequest request) {
        log.error("ApiException: ", ex);
        return buildErrorResponse(ex.getMessage(), ex.getDescription(), HttpStatus.BAD_REQUEST, request);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(MethodArgumentNotValidException ex, HttpServletRequest request) {
        log.error("Validation Error: ", ex);
        List<String> errors = ex.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.toList());
        String message = "Validation failed";
        String description = String.join("; ", errors);
        return buildErrorResponse(message, description, HttpStatus.BAD_REQUEST, request);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadable(HttpMessageNotReadableException ex, HttpServletRequest request) {
        log.error("Malformed JSON request: ", ex);
        return buildErrorResponse("Malformed JSON request", ex.getLocalizedMessage(), HttpStatus.BAD_REQUEST, request);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleMethodNotSupported(HttpRequestMethodNotSupportedException ex, HttpServletRequest request) {
        log.error("Method Not Allowed: ", ex);
        return buildErrorResponse("Method not allowed", ex.getMessage(), HttpStatus.METHOD_NOT_ALLOWED, request);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(ConstraintViolationException ex, HttpServletRequest request) {
        log.error("Constraint Violation: ", ex);
        String description = ex.getConstraintViolations().stream()
                .map(cv -> cv.getPropertyPath() + ": " + cv.getMessage())
                .collect(Collectors.joining("; "));
        return buildErrorResponse("Validation failed", description, HttpStatus.BAD_REQUEST, request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleAllUncaughtExceptions(Exception ex, HttpServletRequest request) {
        log.error("Unhandled Exception: ", ex);
        return buildErrorResponse("Internal Server Error", ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR, request);
    }

    private ResponseEntity<ErrorResponse> buildErrorResponse(String message, String description, HttpStatus status, HttpServletRequest request) {
        ErrorResponse response = ErrorResponse.builder()
                .message(message)
                .description(description)
                .statusCode(status.value())
                .path(request.getRequestURI())
                .timestamp(Instant.now())
                .method(request.getMethod())
                .build();
        return new ResponseEntity<>(response, new HttpHeaders(), status);
    }
}
