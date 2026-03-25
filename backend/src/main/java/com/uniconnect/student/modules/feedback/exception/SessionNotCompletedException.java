package com.uniconnect.student.modules.feedback.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception thrown when attempting to submit feedback for a session that has not been completed.
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class SessionNotCompletedException extends RuntimeException {

    public SessionNotCompletedException(String message) {
        super(message);
    }
}
