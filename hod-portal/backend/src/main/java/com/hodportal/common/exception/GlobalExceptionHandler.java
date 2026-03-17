package com.hodportal.common.exception;

import com.hodportal.common.dto.ApiResponseDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.HashMap;
import java.util.Map;

/**
 * Global exception handler for the HoD Portal application.
 * Catches exceptions thrown across all controllers and returns proper HTTP responses.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handle validation errors from @Valid annotations.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponseDTO<Map<String, String>>> handleValidationExceptions(
            MethodArgumentNotValidException ex) {

        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        ApiResponseDTO<Map<String, String>> response = ApiResponseDTO.error("Validation failed");
        response.setData(errors);

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handle resource not found exceptions.
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponseDTO<Void>> handleResourceNotFoundException(
            ResourceNotFoundException ex) {

        return new ResponseEntity<>(
                ApiResponseDTO.error(ex.getMessage()),
                HttpStatus.NOT_FOUND
        );
    }

    /**
     * Handle unauthorized access exceptions.
     */
    @ExceptionHandler(UnauthorizedAccessException.class)
    public ResponseEntity<ApiResponseDTO<Void>> handleUnauthorizedAccessException(
            UnauthorizedAccessException ex) {

        return new ResponseEntity<>(
                ApiResponseDTO.error(ex.getMessage()),
                HttpStatus.FORBIDDEN
        );
    }

    /**
     * Handle decision already made exceptions (contribution review).
     */
    @ExceptionHandler(DecisionAlreadyMadeException.class)
    public ResponseEntity<ApiResponseDTO<Void>> handleDecisionAlreadyMadeException(
            DecisionAlreadyMadeException ex) {

        return new ResponseEntity<>(
                ApiResponseDTO.error(ex.getMessage()),
                HttpStatus.CONFLICT
        );
    }

    /**
     * Handle illegal argument exceptions.
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponseDTO<Void>> handleIllegalArgumentException(
            IllegalArgumentException ex) {

        return new ResponseEntity<>(
                ApiResponseDTO.error(ex.getMessage()),
                HttpStatus.BAD_REQUEST
        );
    }

    /**
     * Handle missing static resources or endpoints.
     */
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiResponseDTO<Void>> handleNoResourceFoundException(NoResourceFoundException ex) {
        return new ResponseEntity<>(
                ApiResponseDTO.error("Resource not found: " + ex.getResourcePath()),
                HttpStatus.NOT_FOUND
        );
    }

    /**
     * Handle incorrect HTTP methods.
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiResponseDTO<Void>> handleHttpRequestMethodNotSupportedException(
            HttpRequestMethodNotSupportedException ex) {
        return new ResponseEntity<>(
                ApiResponseDTO.error("HTTP method not supported: " + ex.getMethod()),
                HttpStatus.METHOD_NOT_ALLOWED
        );
    }

    /**
     * Handle all other unhandled exceptions.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponseDTO<Void>> handleGeneralException(Exception ex) {

        return new ResponseEntity<>(
                ApiResponseDTO.error("An unexpected error occurred: " + ex.getMessage()),
                HttpStatus.INTERNAL_SERVER_ERROR
        );
    }
}
