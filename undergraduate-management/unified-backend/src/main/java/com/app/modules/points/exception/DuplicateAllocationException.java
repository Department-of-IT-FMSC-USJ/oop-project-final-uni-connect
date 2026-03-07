package com.app.modules.points.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception thrown when points have already been allocated for an activity.
 */
@ResponseStatus(HttpStatus.CONFLICT)
public class DuplicateAllocationException extends RuntimeException {

    public DuplicateAllocationException(String message) {
        super(message);
    }
}
