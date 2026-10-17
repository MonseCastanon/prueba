package com.proyecto.servicios.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Solicitud para baja lógica de cliente en el sistema")
public class ClienteBajaRequest {

    @NotNull(message = "El ID del cliente es obligatorio")
    @Schema(description = "Identificador del cliente a dar de baja", example = "1001", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long id;

    @Schema(description = "Motivo o justificación de la baja", example = "Baja solicitada por el titular")
    private String motivo;
}
