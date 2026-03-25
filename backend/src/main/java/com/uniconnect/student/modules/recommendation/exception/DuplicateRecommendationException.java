package com.uniconnect.student.modules.recommendation.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception thrown when a duplicate recommendation already exists.
 */
@ResponseStatus(HttpStatus.CONFLICT)
public class DuplicateRecommendationException extends RuntimeException {

    public DuplicateRecommendationException(String message) {
        super(message);
    }
}
