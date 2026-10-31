package com.proyecto.servicios.exception;

public class InvalidPaymentException extends BusinessRuleException {

    public InvalidPaymentException(String message) {
        super(message);
    }
}

