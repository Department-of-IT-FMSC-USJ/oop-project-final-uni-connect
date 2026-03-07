package com.app.modules.events.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception thrown when a student attempts to submit a duplicate event participation.
 */
@ResponseStatus(HttpStatus.CONFLICT)
public class DuplicateSubmissionException extends RuntimeException {

    public DuplicateSubmissionException(String message) {
        super(message);
    }
}
