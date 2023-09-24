package com.trafiklab.busroutecalculator.exception;

public class RateLimitExceedException extends RuntimeException {
    private static final long serialVersionUID = 1L;
    public RateLimitExceedException(String message) {
        super(message);
    }
}
