package com.proyecto.servicios.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Solicitud para apertura de nueva cuenta bancaria vía RequestBody")
public class CuentaCreacionRequest {

    @NotNull(message = "El ID del cliente es obligatorio")
    @Schema(description = "Identificador del cliente titular", example = "1001", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long clienteId;

    @DecimalMin(value = "0.0", message = "El saldo inicial no puede ser negativo")
    @Schema(description = "Saldo inicial de apertura (opcional)", example = "100.00")
    private BigDecimal saldoInicial;
}
