package com.academicmentor.modules.allocations.exception;

public class MentorCapacityExceededException extends RuntimeException {
    public MentorCapacityExceededException(String message) {
        super(message);
    }
}
