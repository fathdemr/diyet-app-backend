package com.fatihdemir.diyetappbackend.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class AppointmentException extends RuntimeException {

    private final HttpStatus status;

    public AppointmentException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }
}