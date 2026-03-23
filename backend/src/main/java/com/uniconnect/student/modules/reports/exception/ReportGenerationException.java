package com.uniconnect.student.modules.reports.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception thrown when report generation fails.
 */
@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
public class ReportGenerationException extends RuntimeException {

    public ReportGenerationException(String message) {
        super(message);
    }
}
