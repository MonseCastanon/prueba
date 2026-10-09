package com.proyecto.servicios.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "DTO de Estado con su lista de Municipios/Alcaldías")
public class EstadoMunicipiosDto implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "Nombre del estado", example = "Jalisco")
    private String estado;

    @Schema(description = "Total de municipios en el estado", example = "125")
    private int totalMunicipios;

    @Schema(description = "Listado de nombres de municipios", example = "[\"Guadalajara\", \"Zapopan\", \"Tlaquepaque\"]")
    private List<String> municipios;
}
