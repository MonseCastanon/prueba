package com.proyecto.servicios.util;

public final class Masker {

    private Masker() {
    }

    public static String enmascararCuenta(String cuenta) {
        if (cuenta == null || cuenta.isBlank()) {
            return "****";
        }
        String limpia = cuenta.trim();
        if (limpia.length() <= 4) {
            return "****";
        }
        return "****" + limpia.substring(limpia.length() - 4);
    }

    public static String enmascararReferencia(String referencia) {
        if (referencia == null || referencia.isBlank()) {
            return "****";
        }
        String limpia = referencia.trim();
        if (limpia.length() <= 4) {
            return "****" + limpia;
        }
        return "****" + limpia.substring(limpia.length() - 4);
    }

    public static String enmascararPin(String pin) {
        return "****";
    }
}

