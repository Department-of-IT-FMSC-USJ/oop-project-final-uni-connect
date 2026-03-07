package com.app.modules.feedback.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception thrown when a student attempts to submit duplicate feedback for a session.
 */
@ResponseStatus(HttpStatus.CONFLICT)
public class DuplicateFeedbackException extends RuntimeException {

    public DuplicateFeedbackException(String message) {
        super(message);
    }
}
