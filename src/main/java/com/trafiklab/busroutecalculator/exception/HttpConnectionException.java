package com.trafiklab.busroutecalculator.exception;

public class HttpConnectionException extends RuntimeException {
    private static final long serialVersionUID = 1L;
    public HttpConnectionException(String message) {
        super(message);
    }

}
