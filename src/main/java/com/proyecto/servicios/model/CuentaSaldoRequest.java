package com.proyecto.servicios.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Solicitud para consultar saldo de una cuenta bancaria vía RequestBody")
public class CuentaSaldoRequest {

    @NotBlank(message = "El número de cuenta es obligatorio")
    @Schema(description = "Número de cuenta bancaria", example = "9080512384", requiredMode = Schema.RequiredMode.REQUIRED)
    private String numeroCuenta;
}
