package com.proyecto.servicios.enums;

import java.util.Set;

public enum EstadoTransaccion {
    PENDIENTE,
    ENVIADA,
    EXITOSA,
    RECHAZADA,
    REVERSADA,
    INCIERTA,
    REVISION_MANUAL;

    public boolean puedePasarA(EstadoTransaccion nuevo) {
        if (this == nuevo) {
            return true;
        }
        return switch (this) {
            case PENDIENTE -> nuevo == ENVIADA || nuevo == REVERSADA || nuevo == RECHAZADA;
            case ENVIADA -> nuevo == EXITOSA || nuevo == REVERSADA || nuevo == RECHAZADA || nuevo == INCIERTA;
            case INCIERTA -> nuevo == EXITOSA || nuevo == REVERSADA || nuevo == REVISION_MANUAL;
            case REVISION_MANUAL -> nuevo == EXITOSA || nuevo == REVERSADA;
            case EXITOSA, REVERSADA, RECHAZADA -> false; // Estados terminales
        };
    }

    public boolean esTerminal() {
        return this == EXITOSA || this == REVERSADA || this == RECHAZADA;
    }
}

