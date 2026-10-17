package com.proyecto.servicios.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.OffsetDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Respuesta estructurada con los datos públicos del usuario (sin credenciales ni hash)")
public class UsuarioResponse {

    @Schema(description = "Identificador único del usuario", example = "1")
    private Long id;

    @Schema(description = "Correo electrónico institucional o personal", example = "usuario@empresa.com")
    private String correo;

    @Schema(description = "Rol asignado en el sistema", example = "ROLE_CLIENTE")
    private String rol;

    @Schema(description = "Estatus de activación del usuario", example = "true")
    private Boolean activo;

    @Schema(description = "Identificador del cliente asociado", example = "1001")
    private Long clienteId;

    @Schema(description = "Fecha y hora de creación del registro en el servidor", example = "2026-03-15T10:30:00Z")
    private OffsetDateTime fechaCreacion;

    @Schema(description = "Fecha y hora de última modificación del registro", example = "2026-03-15T12:00:00Z")
    private OffsetDateTime fechaActualizacion;
}
