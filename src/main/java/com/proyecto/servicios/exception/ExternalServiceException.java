package com.proyecto.servicios.exception;

import lombok.Getter;

@Getter
public class ExternalServiceException extends RuntimeException {

    private final int statusCode;

    public ExternalServiceException(String message, int statusCode) {
        super(message);
        this.statusCode = statusCode;
    }

    public ExternalServiceException(String message, Throwable cause, int statusCode) {
        super(message, cause);
        this.statusCode = statusCode;
    }
}
