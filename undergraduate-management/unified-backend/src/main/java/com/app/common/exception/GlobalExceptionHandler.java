package com.app.common.exception;

import com.app.common.dto.ApiResponseDTO;
import com.app.modules.events.exception.DuplicateSubmissionException;
import com.app.modules.points.exception.DuplicateAllocationException;
import com.app.modules.mentor.exception.InsufficientPointsException;
import com.app.modules.feedback.exception.DuplicateFeedbackException;
import com.app.modules.feedback.exception.SessionNotCompletedException;
import com.app.modules.recommendation.exception.DuplicateRecommendationException;
import com.app.modules.recommendation.exception.ProfileIncompleteException;
import com.app.modules.reports.exception.InvalidDateRangeException;
import com.app.modules.reports.exception.ReportGenerationException;
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
 * Global exception handler for the unified application.
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
     * Handle duplicate submission exceptions (events module).
     */
    @ExceptionHandler(DuplicateSubmissionException.class)
    public ResponseEntity<ApiResponseDTO<Void>> handleDuplicateSubmissionException(
            DuplicateSubmissionException ex) {

        return new ResponseEntity<>(
                ApiResponseDTO.error(ex.getMessage()),
                HttpStatus.CONFLICT
        );
    }

    /**
     * Handle duplicate allocation exceptions (points module).
     */
    @ExceptionHandler(DuplicateAllocationException.class)
    public ResponseEntity<ApiResponseDTO<Void>> handleDuplicateAllocationException(
            DuplicateAllocationException ex) {

        return new ResponseEntity<>(
                ApiResponseDTO.error(ex.getMessage()),
                HttpStatus.CONFLICT
        );
    }

    /**
     * Handle insufficient points exceptions (mentor module).
     */
    @ExceptionHandler(InsufficientPointsException.class)
    public ResponseEntity<ApiResponseDTO<Void>> handleInsufficientPointsException(
            InsufficientPointsException ex) {

        return new ResponseEntity<>(
                ApiResponseDTO.error(ex.getMessage()),
                HttpStatus.FORBIDDEN
        );
    }

    /**
     * Handle duplicate feedback exceptions (feedback module).
     */
    @ExceptionHandler(DuplicateFeedbackException.class)
    public ResponseEntity<ApiResponseDTO<Void>> handleDuplicateFeedbackException(
            DuplicateFeedbackException ex) {

        return new ResponseEntity<>(
                ApiResponseDTO.error(ex.getMessage()),
                HttpStatus.CONFLICT
        );
    }

    /**
     * Handle session not completed exceptions (feedback module).
     */
    @ExceptionHandler(SessionNotCompletedException.class)
    public ResponseEntity<ApiResponseDTO<Void>> handleSessionNotCompletedException(
            SessionNotCompletedException ex) {

        return new ResponseEntity<>(
                ApiResponseDTO.error(ex.getMessage()),
                HttpStatus.BAD_REQUEST
        );
    }

    /**
     * Handle profile incomplete exceptions (recommendation module).
     */
    @ExceptionHandler(ProfileIncompleteException.class)
    public ResponseEntity<ApiResponseDTO<Void>> handleProfileIncompleteException(
            ProfileIncompleteException ex) {

        return new ResponseEntity<>(
                ApiResponseDTO.error(ex.getMessage()),
                HttpStatus.BAD_REQUEST
        );
    }

    /**
     * Handle invalid date range exceptions (reports module).
     */
    @ExceptionHandler(InvalidDateRangeException.class)
    public ResponseEntity<ApiResponseDTO<Void>> handleInvalidDateRangeException(
            InvalidDateRangeException ex) {

        return new ResponseEntity<>(
                ApiResponseDTO.error(ex.getMessage()),
                HttpStatus.BAD_REQUEST
        );
    }

    /**
     * Handle duplicate recommendation exceptions (recommendation module).
     */
    @ExceptionHandler(DuplicateRecommendationException.class)
    public ResponseEntity<ApiResponseDTO<Void>> handleDuplicateRecommendationException(
            DuplicateRecommendationException ex) {

        return new ResponseEntity<>(
                ApiResponseDTO.error(ex.getMessage()),
                HttpStatus.CONFLICT
        );
    }

    /**
     * Handle report generation exceptions (reports module).
     */
    @ExceptionHandler(ReportGenerationException.class)
    public ResponseEntity<ApiResponseDTO<Void>> handleReportGenerationException(
            ReportGenerationException ex) {

        return new ResponseEntity<>(
                ApiResponseDTO.error(ex.getMessage()),
                HttpStatus.INTERNAL_SERVER_ERROR
        );
    }

    /**
     * Handle illegal state exceptions.
     */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ApiResponseDTO<Void>> handleIllegalStateException(
            IllegalStateException ex) {

        return new ResponseEntity<>(
                ApiResponseDTO.error(ex.getMessage()),
                HttpStatus.CONFLICT
        );
    }

    /**
     * Handle file size exceeded exceptions.
     */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ApiResponseDTO<Void>> handleMaxSizeException(
            MaxUploadSizeExceededException ex) {

        return new ResponseEntity<>(
                ApiResponseDTO.error("File size exceeds the maximum allowed size"),
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
