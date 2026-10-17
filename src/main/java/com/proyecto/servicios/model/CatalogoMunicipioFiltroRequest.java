package com.proyecto.servicios.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Filtro para consultar municipios por estado")
public class CatalogoMunicipioFiltroRequest {

    @NotBlank(message = "El nombre, clave INEGI o abreviatura del estado es obligatorio")
    @Schema(description = "Nombre, clave INEGI o abreviatura del estado", example = "Jalisco", requiredMode = Schema.RequiredMode.REQUIRED)
    private String estado;
}
