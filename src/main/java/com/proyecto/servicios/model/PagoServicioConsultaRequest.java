package com.proyecto.servicios.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Solicitud para consulta del estatus de una transacción de pago de servicio")
public record PagoServicioConsultaRequest(
        @NotBlank(message = "El IdDocumento es obligatorio para la consulta")
        @Schema(description = "Llave de idempotencia del cliente utilizada al pagar", example = "DOC-2026-998811", requiredMode = Schema.RequiredMode.REQUIRED)
        String idDocumento,

        @Schema(description = "Número de cuenta bancaria opcional", example = "9080512384")
        String numeroCuenta
) {
}

