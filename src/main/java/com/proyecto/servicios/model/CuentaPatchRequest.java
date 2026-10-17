package com.proyecto.servicios.model;

import com.proyecto.servicios.enums.EstatusCuenta;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Solicitud para actualización parcial de cuenta bancaria vía RequestBody")
public class CuentaPatchRequest {

    @NotBlank(message = "El número de cuenta es obligatorio")
    @Schema(description = "Número de cuenta bancaria a actualizar", example = "9080512384", requiredMode = Schema.RequiredMode.REQUIRED)
    private String numeroCuenta;

    @Schema(description = "Nuevo estatus de la cuenta", example = "ACTIVA")
    private EstatusCuenta estatus;

    @DecimalMin(value = "0.00", message = "El saldo no puede ser negativo")
    @Schema(description = "Saldo disponible", example = "5000.00")
    private BigDecimal saldo;

    @Size(max = 255, message = "El motivo no puede exceder 255 caracteres")
    @Schema(description = "Justificación del cambio", example = "Ajuste contable autorizado")
    private String motivo;
}
