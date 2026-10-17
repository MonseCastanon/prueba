package com.proyecto.servicios.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Solicitud para baja lógica o eliminación controlada de usuario")
public class UsuarioEliminaRequest {

    @NotNull(message = "El ID del usuario es obligatorio")
    @Schema(description = "Identificador único del usuario a dar de baja", example = "5", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long id;

    @NotBlank(message = "El motivo de la baja es obligatorio")
    @Size(max = 255, message = "El motivo no puede exceder los 255 caracteres")
    @Schema(description = "Justificación o motivo de negocio para la inhabilitación del usuario", example = "Baja solicitada por término de contrato laboral", requiredMode = Schema.RequiredMode.REQUIRED)
    private String motivo;
}
