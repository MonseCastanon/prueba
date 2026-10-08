package com.proyecto.servicios.model;

import lombok.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CuentaResponse {
    private Long id;
    private Long clienteId;
    private String nombreTitular;
    private String numeroCuenta;
    private BigDecimal saldo;
    private String estatus;
    private Boolean activa;
    private OffsetDateTime fechaCreacion;
    private OffsetDateTime fechaBloqueo;
    private String motivoBloqueo;
}
