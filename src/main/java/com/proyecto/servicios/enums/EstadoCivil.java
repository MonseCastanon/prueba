package com.proyecto.servicios.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum EstadoCivil {
    SOLTERO,
    CASADO,
    DIVORCIADO,
    VIUDO,
    UNION_LIBRE;

    @JsonCreator
    public static EstadoCivil fromString(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        String normalized = value.trim().toUpperCase().replace(" ", "_");
        for (EstadoCivil ec : EstadoCivil.values()) {
            if (ec.name().equals(normalized)) {
                return ec;
            }
        }
        throw new IllegalArgumentException("Estado civil inválido: '" + value + "'. Valores permitidos: SOLTERO, CASADO, DIVORCIADO, VIUDO, UNION_LIBRE");
    }

    @JsonValue
    public String toValue() {
        return this.name();
    }
}
