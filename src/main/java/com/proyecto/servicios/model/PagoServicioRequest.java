package com.proyecto.servicios.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.ToString;

import java.math.BigDecimal;

@Schema(description = "Solicitud para ejecución de pago de servicio o recarga en Gestopago")
public record PagoServicioRequest(
        @NotBlank(message = "El número de cuenta es obligatorio")
        @Pattern(regexp = "\\d{10,20}", message = "El número de cuenta debe tener entre 10 y 20 dígitos")
        @Schema(description = "Número de cuenta origen de los fondos", example = "9080512384", requiredMode = Schema.RequiredMode.REQUIRED)
        String numeroCuenta,

        @NotNull(message = "El ID de servicio es obligatorio")
        @Positive(message = "El ID de servicio debe ser positivo")
        @Schema(description = "Identificador de servicio en Gestopago", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
        Integer idServicio,

        @NotNull(message = "El ID de producto es obligatorio")
        @Positive(message = "El ID de producto debe ser positivo")
        @Schema(description = "Identificador de producto en Gestopago", example = "101", requiredMode = Schema.RequiredMode.REQUIRED)
        Integer idProducto,

        @NotBlank(message = "El IdDocumento es obligatorio")
        @Size(min = 8, max = 40, message = "El IdDocumento debe tener entre 8 y 40 caracteres")
        @Schema(description = "Llave de idempotencia del cliente", example = "DOC-2026-998811", requiredMode = Schema.RequiredMode.REQUIRED)
        String idDocumento,

        @NotBlank(message = "La referencia o teléfono es obligatoria")
        @Size(max = 100, message = "La referencia no puede exceder 100 caracteres")
        @Schema(description = "Teléfono celular (recargas) o número de contrato/servicio (pago de luz, agua, etc.)", example = "5512345678", requiredMode = Schema.RequiredMode.REQUIRED)
        String referencia,

        @DecimalMin(value = "0.01", message = "El monto mínimo a pagar es $0.01")
        @Digits(integer = 13, fraction = 2, message = "El monto admite máximo 2 decimales")
        @Schema(description = "Monto a pagar (obligatorio para servicios tipoFront=2; ignorado para recargas tipoFront=1)", example = "150.00")
        BigDecimal monto,

        @NotBlank(message = "El PIN transaccional es obligatorio")
        @Pattern(regexp = "\\d{4,6}", message = "El PIN debe tener entre 4 y 6 dígitos")
        @ToString.Exclude
        @Schema(description = "PIN transaccional seguro del cliente", example = "1234", requiredMode = Schema.RequiredMode.REQUIRED)
        String pin
) {
}

