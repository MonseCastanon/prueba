package com.proyecto.servicios.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Datos de ubicación geográfica correspondientes a un código postal")
public class CodigoPostalResponse {

    @Schema(description = "Identificador único", example = "10")
    private Long id;

    @Schema(description = "Código postal de 5 dígitos", example = "03940")
    private String codigoPostal;

    @Schema(description = "Municipio o alcaldía", example = "Benito Juárez")
    private String municipioAlcaldia;

    @Schema(description = "Nombre del asentamiento o colonia", example = "Crédito Constructor")
    private String colonia;

    @Schema(description = "Nombre del estado asociado", example = "Ciudad de México")
    private String estadoNombre;

    @Schema(description = "Estatus de activación", example = "true")
    private Boolean activo;
}
