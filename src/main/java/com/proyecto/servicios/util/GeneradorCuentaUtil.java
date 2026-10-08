package com.proyecto.servicios.util;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;

@Component
public class GeneradorCuentaUtil {

    private static final SecureRandom RANDOM = new SecureRandom();

    public String generarNumeroCuenta(String prefijo) {
        int numeroAleatorio = 100000 + RANDOM.nextInt(900000);
        return prefijo + numeroAleatorio;
    }
}
