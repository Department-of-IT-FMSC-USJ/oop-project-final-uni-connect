package com.uniconnect.student.modules.reports.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception thrown when a report date range is invalid.
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InvalidDateRangeException extends RuntimeException {

    public InvalidDateRangeException(String message) {
        super(message);
    }
}
