package com.app.modules.mentor.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception thrown when a student does not have enough contribution points
 * to connect with an Industry mentor.
 */
@ResponseStatus(HttpStatus.FORBIDDEN)
public class InsufficientPointsException extends RuntimeException {

    public InsufficientPointsException(String message) {
        super(message);
    }
}
