package com.trafiklab.busroutecalculator.exception;

public class InvalidApiKeyException extends RuntimeException {
    private static final long serialVersionUID = 1L;
    public InvalidApiKeyException(String message) {
        super(message);
    }
}
