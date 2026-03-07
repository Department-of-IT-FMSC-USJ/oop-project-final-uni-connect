package com.uniconnect.exception;

import com.uniconnect.dto.ApiResponseDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.util.HashMap;
import java.util.Map;

/**
 * Global exception handler for the application.
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
     * Handle file storage exceptions.
     */
    @ExceptionHandler(FileStorageException.class)
    public ResponseEntity<ApiResponseDTO<Void>> handleFileStorageException(
            FileStorageException ex) {

        return new ResponseEntity<>(
                ApiResponseDTO.error(ex.getMessage()),
                HttpStatus.INTERNAL_SERVER_ERROR
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
     * Handle invalid file exceptions.
     */
    @ExceptionHandler(InvalidFileException.class)
    public ResponseEntity<ApiResponseDTO<Void>> handleInvalidFileException(
            InvalidFileException ex) {

        return new ResponseEntity<>(
                ApiResponseDTO.error(ex.getMessage()),
                HttpStatus.BAD_REQUEST
        );
    }

    /**
     * Handle file size exceeded exceptions.
     */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ApiResponseDTO<Void>> handleMaxSizeException(
            MaxUploadSizeExceededException ex) {

        return new ResponseEntity<>(
                ApiResponseDTO.error("File size exceeds the maximum allowed size of 20MB"),
                HttpStatus.BAD_REQUEST
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
