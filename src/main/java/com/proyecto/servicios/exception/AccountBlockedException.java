package com.proyecto.servicios.exception;

public class AccountBlockedException extends RuntimeException {
    public AccountBlockedException(String mensaje) {
        super(mensaje);
    }
}
