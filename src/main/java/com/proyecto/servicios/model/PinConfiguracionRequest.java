package com.proyecto.servicios.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record PinConfiguracionRequest(
        @NotBlank(message = "El PIN transaccional es obligatorio")
        @Pattern(regexp = "^\\d{4,6}$", message = "El PIN transaccional debe contener entre 4 y 6 dígitos numéricos")
        @Schema(description = "Nuevo PIN transaccional de 4 a 6 dígitos", example = "4829")
        String pin
) {
}

