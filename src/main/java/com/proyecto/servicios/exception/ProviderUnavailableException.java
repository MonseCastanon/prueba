package com.proyecto.servicios.exception;

public class ProviderUnavailableException extends ExternalServiceException {

    public ProviderUnavailableException(String message) {
        super(message, 503);
    }

    public ProviderUnavailableException(String message, Throwable cause) {
        super(message, cause, 503);
    }
}

