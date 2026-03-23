package com.uniconnect.academic.modules.sessions.exception;

public class DuplicateSessionException extends RuntimeException {
    public DuplicateSessionException(String message) {
        super(message);
    }
}
