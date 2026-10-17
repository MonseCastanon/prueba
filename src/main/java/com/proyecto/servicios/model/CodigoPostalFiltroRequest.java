package com.proyecto.servicios.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Filtro para consultar información de ubicación por Código Postal")
public class CodigoPostalFiltroRequest {

    @NotBlank(message = "El código postal es obligatorio")
    @Pattern(regexp = "^\\d{5}$", message = "El código postal debe constar de 5 dígitos numéricos")
    @Schema(description = "Código postal de 5 dígitos", example = "03940", requiredMode = Schema.RequiredMode.REQUIRED)
    private String codigoPostal;
}
