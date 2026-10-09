package com.proyecto.servicios.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "DTO de información de Estado de la República Mexicana")
public class EstadoDto implements Serializable {

    private static final long serialVersionUID = 1L;

    @JsonProperty("cve_ent")
    @Schema(description = "Clave de entidad según INEGI (ej. 01, 09, 14)", example = "09")
    private String claveEntidad;

    @JsonProperty("clave")
    @Schema(description = "Abreviatura oficial del estado", example = "CDMX")
    private String clave;

    @JsonProperty("nombre")
    @Schema(description = "Nombre oficial del estado", example = "CIUDAD DE MÉXICO")
    private String nombre;
}
