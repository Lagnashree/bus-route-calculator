package com.trafiklab.busroutecalculator.exception;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.*;


@RestControllerAdvice
public class CustomExceptionHandler {
    @ExceptionHandler(value= HttpConnectionException.class)
    public ResponseEntity<CustomErrorResponse> handleHttpConnectionException(HttpConnectionException e) {
        CustomErrorResponse error = new CustomErrorResponse( e.getMessage());
        error.setTimestamp(LocalDateTime.now());
        error.setStatus((HttpStatus.INTERNAL_SERVER_ERROR.value()));
        error.setPath("/api/v1/busline");
        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public Object handleRequestValidationException(Exception ex, HttpServletRequest request) {
        CustomErrorResponse error = new CustomErrorResponse( ex.getMessage());
        error.setTimestamp(LocalDateTime.now());
        error.setStatus((HttpStatus.BAD_REQUEST.value()));
        error.setPath("/api/v1/busline");
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }
}
