package com.proyecto.servicios.model;

import com.proyecto.servicios.enums.EstatusCuenta;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CuentaPatchRequest {
    private EstatusCuenta estatus;

    @DecimalMin(value = "0.00", message = "El saldo no puede ser negativo")
    private BigDecimal saldo;

    @Size(max = 255, message = "El motivo no puede exceder 255 caracteres")
    private String motivo;
}
