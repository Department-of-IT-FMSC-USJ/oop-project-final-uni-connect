package com.uniconnect.common.exception;

import com.uniconnect.student.common.dto.ApiResponseDTO;
import com.uniconnect.student.modules.events.exception.DuplicateSubmissionException;
import com.uniconnect.student.modules.feedback.exception.DuplicateFeedbackException;
import com.uniconnect.student.modules.feedback.exception.SessionNotCompletedException;
import com.uniconnect.student.modules.mentor.exception.InsufficientPointsException;
import com.uniconnect.student.modules.points.exception.DuplicateAllocationException;
import com.uniconnect.student.modules.recommendation.exception.DuplicateRecommendationException;
import com.uniconnect.student.modules.recommendation.exception.ProfileIncompleteException;
import com.uniconnect.student.modules.reports.exception.InvalidDateRangeException;
import com.uniconnect.student.modules.reports.exception.ReportGenerationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.HashMap;
import java.util.Map;

/**
 * Single REST exception handler for the monolith (replaces per-module GlobalExceptionHandler beans).
 */
@RestControllerAdvice
public class UnifiedGlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponseDTO<Map<String, String>>> handleValidationExceptions(
            MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            errors.put(fieldName, error.getDefaultMessage());
        });
        ApiResponseDTO<Map<String, String>> response = ApiResponseDTO.error("Validation failed");
        response.setData(errors);
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(com.uniconnect.student.common.exception.ResourceNotFoundException.class)
    public ResponseEntity<ApiResponseDTO<Void>> handleStudentResourceNotFound(
            com.uniconnect.student.common.exception.ResourceNotFoundException ex) {
        return new ResponseEntity<>(ApiResponseDTO.error(ex.getMessage()), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(com.uniconnect.academic.common.exception.ResourceNotFoundException.class)
    public ResponseEntity<ApiResponseDTO<Void>> handleAcademicResourceNotFound(
            com.uniconnect.academic.common.exception.ResourceNotFoundException ex) {
        return new ResponseEntity<>(ApiResponseDTO.error(ex.getMessage()), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(com.uniconnect.hod.common.exception.ResourceNotFoundException.class)
    public ResponseEntity<ApiResponseDTO<Void>> handleHodResourceNotFound(
            com.uniconnect.hod.common.exception.ResourceNotFoundException ex) {
        return new ResponseEntity<>(ApiResponseDTO.error(ex.getMessage()), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(com.uniconnect.industry.common.exception.ResourceNotFoundException.class)
    public ResponseEntity<ApiResponseDTO<Void>> handleIndustryResourceNotFound(
            com.uniconnect.industry.common.exception.ResourceNotFoundException ex) {
        return new ResponseEntity<>(ApiResponseDTO.error(ex.getMessage()), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(com.uniconnect.student.common.exception.FileStorageException.class)
    public ResponseEntity<ApiResponseDTO<Void>> handleFileStorage(
            com.uniconnect.student.common.exception.FileStorageException ex) {
        return new ResponseEntity<>(ApiResponseDTO.error(ex.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(com.uniconnect.student.common.exception.UnauthorizedAccessException.class)
    public ResponseEntity<ApiResponseDTO<Void>> handleStudentUnauthorized(
            com.uniconnect.student.common.exception.UnauthorizedAccessException ex) {
        return new ResponseEntity<>(ApiResponseDTO.error(ex.getMessage()), HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(com.uniconnect.academic.common.exception.UnauthorizedAccessException.class)
    public ResponseEntity<ApiResponseDTO<Void>> handleAcademicUnauthorized(
            com.uniconnect.academic.common.exception.UnauthorizedAccessException ex) {
        return new ResponseEntity<>(ApiResponseDTO.error(ex.getMessage()), HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(com.uniconnect.hod.common.exception.UnauthorizedAccessException.class)
    public ResponseEntity<ApiResponseDTO<Void>> handleHodUnauthorized(
            com.uniconnect.hod.common.exception.UnauthorizedAccessException ex) {
        return new ResponseEntity<>(ApiResponseDTO.error(ex.getMessage()), HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(com.uniconnect.industry.common.exception.UnauthorizedAccessException.class)
    public ResponseEntity<ApiResponseDTO<Void>> handleIndustryUnauthorized(
            com.uniconnect.industry.common.exception.UnauthorizedAccessException ex) {
        return new ResponseEntity<>(ApiResponseDTO.error(ex.getMessage()), HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(com.uniconnect.student.common.exception.InvalidFileException.class)
    public ResponseEntity<ApiResponseDTO<Void>> handleInvalidFile(
            com.uniconnect.student.common.exception.InvalidFileException ex) {
        return new ResponseEntity<>(ApiResponseDTO.error(ex.getMessage()), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(DuplicateSubmissionException.class)
    public ResponseEntity<ApiResponseDTO<Void>> handleDuplicateSubmission(DuplicateSubmissionException ex) {
        return new ResponseEntity<>(ApiResponseDTO.error(ex.getMessage()), HttpStatus.CONFLICT);
    }

    @ExceptionHandler(DuplicateAllocationException.class)
    public ResponseEntity<ApiResponseDTO<Void>> handleStudentDuplicateAllocation(DuplicateAllocationException ex) {
        return new ResponseEntity<>(ApiResponseDTO.error(ex.getMessage()), HttpStatus.CONFLICT);
    }

    @ExceptionHandler(com.uniconnect.academic.modules.allocations.exception.DuplicateAllocationException.class)
    public ResponseEntity<ApiResponseDTO<Void>> handleAcademicDuplicateAllocation(
            com.uniconnect.academic.modules.allocations.exception.DuplicateAllocationException ex) {
        return new ResponseEntity<>(ApiResponseDTO.error(ex.getMessage()), HttpStatus.CONFLICT);
    }

    @ExceptionHandler(com.uniconnect.academic.modules.allocations.exception.MentorCapacityExceededException.class)
    public ResponseEntity<ApiResponseDTO<Void>> handleMentorCapacityExceeded(
            com.uniconnect.academic.modules.allocations.exception.MentorCapacityExceededException ex) {
        return new ResponseEntity<>(ApiResponseDTO.error(ex.getMessage()), HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(InsufficientPointsException.class)
    public ResponseEntity<ApiResponseDTO<Void>> handleInsufficientPoints(InsufficientPointsException ex) {
        return new ResponseEntity<>(ApiResponseDTO.error(ex.getMessage()), HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(DuplicateFeedbackException.class)
    public ResponseEntity<ApiResponseDTO<Void>> handleDuplicateFeedback(DuplicateFeedbackException ex) {
        return new ResponseEntity<>(ApiResponseDTO.error(ex.getMessage()), HttpStatus.CONFLICT);
    }

    @ExceptionHandler(SessionNotCompletedException.class)
    public ResponseEntity<ApiResponseDTO<Void>> handleSessionNotCompleted(SessionNotCompletedException ex) {
        return new ResponseEntity<>(ApiResponseDTO.error(ex.getMessage()), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ProfileIncompleteException.class)
    public ResponseEntity<ApiResponseDTO<Void>> handleProfileIncomplete(ProfileIncompleteException ex) {
        return new ResponseEntity<>(ApiResponseDTO.error(ex.getMessage()), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(InvalidDateRangeException.class)
    public ResponseEntity<ApiResponseDTO<Void>> handleInvalidDateRange(InvalidDateRangeException ex) {
        return new ResponseEntity<>(ApiResponseDTO.error(ex.getMessage()), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(DuplicateRecommendationException.class)
    public ResponseEntity<ApiResponseDTO<Void>> handleDuplicateRecommendation(DuplicateRecommendationException ex) {
        return new ResponseEntity<>(ApiResponseDTO.error(ex.getMessage()), HttpStatus.CONFLICT);
    }

    @ExceptionHandler(ReportGenerationException.class)
    public ResponseEntity<ApiResponseDTO<Void>> handleReportGeneration(ReportGenerationException ex) {
        return new ResponseEntity<>(ApiResponseDTO.error(ex.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ApiResponseDTO<Void>> handleIllegalState(IllegalStateException ex) {
        return new ResponseEntity<>(ApiResponseDTO.error(ex.getMessage()), HttpStatus.CONFLICT);
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ApiResponseDTO<Void>> handleMaxSize(MaxUploadSizeExceededException ex) {
        return new ResponseEntity<>(
                ApiResponseDTO.error("File size exceeds the maximum allowed size"),
                HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(com.uniconnect.academic.modules.sessions.exception.DuplicateSessionException.class)
    public ResponseEntity<ApiResponseDTO<Void>> handleAcademicDuplicateSession(
            com.uniconnect.academic.modules.sessions.exception.DuplicateSessionException ex) {
        return new ResponseEntity<>(ApiResponseDTO.error(ex.getMessage()), HttpStatus.CONFLICT);
    }

    @ExceptionHandler(com.uniconnect.industry.common.exception.DuplicateSessionException.class)
    public ResponseEntity<ApiResponseDTO<Void>> handleIndustryDuplicateSession(
            com.uniconnect.industry.common.exception.DuplicateSessionException ex) {
        return new ResponseEntity<>(ApiResponseDTO.error(ex.getMessage()), HttpStatus.CONFLICT);
    }

    @ExceptionHandler(com.uniconnect.industry.common.exception.DuplicateSuggestionException.class)
    public ResponseEntity<ApiResponseDTO<Void>> handleDuplicateSuggestion(
            com.uniconnect.industry.common.exception.DuplicateSuggestionException ex) {
        return new ResponseEntity<>(ApiResponseDTO.error(ex.getMessage()), HttpStatus.CONFLICT);
    }

    @ExceptionHandler(com.uniconnect.hod.common.exception.DecisionAlreadyMadeException.class)
    public ResponseEntity<ApiResponseDTO<Void>> handleDecisionAlreadyMade(
            com.uniconnect.hod.common.exception.DecisionAlreadyMadeException ex) {
        return new ResponseEntity<>(ApiResponseDTO.error(ex.getMessage()), HttpStatus.CONFLICT);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponseDTO<Void>> handleIllegalArgument(IllegalArgumentException ex) {
        return new ResponseEntity<>(ApiResponseDTO.error(ex.getMessage()), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiResponseDTO<Void>> handleNoResourceFound(NoResourceFoundException ex) {
        return new ResponseEntity<>(
                ApiResponseDTO.error("Resource not found: " + ex.getResourcePath()),
                HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiResponseDTO<Void>> handleMethodNotSupported(
            HttpRequestMethodNotSupportedException ex) {
        return new ResponseEntity<>(
                ApiResponseDTO.error("HTTP method not supported: " + ex.getMethod()),
                HttpStatus.METHOD_NOT_ALLOWED);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponseDTO<Void>> handleGeneral(Exception ex) {
        return new ResponseEntity<>(
                ApiResponseDTO.error("An unexpected error occurred: " + ex.getMessage()),
                HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
