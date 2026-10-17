package com.proyecto.servicios.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Datos públicos de un estado federativo")
public class EstadoCatalogoResponse {

    @Schema(description = "Identificador único", example = "1")
    private Long id;

    @Schema(description = "Clave oficial del estado", example = "JAL")
    private String claveEstado;

    @Schema(description = "Nombre del estado", example = "Jalisco")
    private String nombre;

    @Schema(description = "Código ISO del país asociado", example = "MEX")
    private String codigoPaisIso;

    @Schema(description = "Estatus de activación", example = "true")
    private Boolean activo;
}
