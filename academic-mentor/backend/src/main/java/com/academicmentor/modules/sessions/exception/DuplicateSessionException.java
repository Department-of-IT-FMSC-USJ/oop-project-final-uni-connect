package com.academicmentor.modules.sessions.exception;

public class DuplicateSessionException extends RuntimeException {
    public DuplicateSessionException(String message) {
        super(message);
    }
}
