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
public class BloqueoDesbloqueoCuentaRequest {

    @NotBlank(message = "El motivo del cambio de estado es obligatorio")
    @Size(max = 255, message = "El motivo no puede exceder 255 caracteres")
    @Schema(example = "Actividad sospechosa detectada por motor antifraude")
    private String motivo;

    @NotBlank(message = "El usuario u operador responsable es obligatorio")
    @Size(max = 100, message = "El usuario operador no puede exceder 100 caracteres")
    @Schema(example = "ejecutivo_antifraude_01")
    private String usuarioOperador;
}
