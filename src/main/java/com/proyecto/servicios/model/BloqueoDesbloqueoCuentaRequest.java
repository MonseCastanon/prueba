package com.proyecto.servicios.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Solicitud para bloqueo o desbloqueo de cuenta bancaria vía RequestBody")
public class BloqueoDesbloqueoCuentaRequest {

    @NotBlank(message = "El número de cuenta es obligatorio")
    @Schema(description = "Número de cuenta a bloquear o desbloquear", example = "9080512384", requiredMode = Schema.RequiredMode.REQUIRED)
    private String numeroCuenta;

    @NotBlank(message = "El motivo del cambio de estado es obligatorio")
    @Size(max = 255, message = "El motivo no puede exceder 255 caracteres")
    @Schema(description = "Motivo del cambio de estado", example = "Actividad sospechosa detectada por motor antifraude", requiredMode = Schema.RequiredMode.REQUIRED)
    private String motivo;

    @NotBlank(message = "El usuario u operador responsable es obligatorio")
    @Size(max = 100, message = "El usuario operador no puede exceder 100 caracteres")
    @Schema(description = "Identificador del usuario u operador responsable", example = "ejecutivo_antifraude_01", requiredMode = Schema.RequiredMode.REQUIRED)
    private String usuarioOperador;

    public BloqueoDesbloqueoCuentaRequest(String motivo, String usuarioOperador) {
        this.motivo = motivo;
        this.usuarioOperador = usuarioOperador;
    }
}
