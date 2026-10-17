package com.proyecto.servicios.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Datos públicos de un país en catálogo")
public class PaisResponse {

    @Schema(description = "Identificador único", example = "1")
    private Long id;

    @Schema(description = "Código ISO Alfa-3 del país", example = "MEX")
    private String codigoIsoAlfa3;

    @Schema(description = "Nombre oficial del país", example = "México")
    private String nombre;

    @Schema(description = "Estatus activo en el catálogo", example = "true")
    private Boolean activo;
}
