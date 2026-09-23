package com.proyecto.servicios.enums;

import lombok.Getter;

@Getter
public enum TipoFrontEnum {
    RECARGA_TELEFONO(1, "Teléfono es el único campo obligatorio (Recarga)"),
    PAGO_REFERENCIA(2, "Número de referencia es obligatorio (Pago de Servicios)"),
    VERIFICAR_REFERENCIA(4, "Usado para verificar detalles de una referencia"),
    CONSULTA_PAGOS(5, "Recupera pagos relacionados a una referencia"),
    CASH_IN(30, "Operaciones Cash-In"),
    CASH_OUT(31, "Operaciones Cash-Out");

    private final int id;
    private final String descripcion;

    TipoFrontEnum(int id, String descripcion) {
        this.id = id;
        this.descripcion = descripcion;
    }

    public static TipoFrontEnum fromId(Integer id) {
        if (id == null) return null;
        for (TipoFrontEnum tipo : values()) {
            if (tipo.id == id) return tipo;
        }
        return null;
    }
}
