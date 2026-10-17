package com.proyecto.servicios.model;

import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.format.annotation.DateTimeFormat;
import lombok.*;

import java.time.OffsetDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Criterios opcionales para la consulta dinámica multicriterio de clientes sin QueryParams")
public class ClienteBusquedaRequest {

    @Schema(description = "Nombre de pila del cliente", example = "Juan")
    private String nombre;

    @Schema(description = "Primer apellido", example = "Pérez")
    private String apellidoPaterno;

    @Schema(description = "Segundo apellido", example = "López")
    private String apellidoMaterno;

    @Schema(description = "CURP del cliente", example = "PELJ950815HDFRPR01")
    private String curp;

    @Schema(description = "RFC del cliente", example = "PELJ950815XXX")
    private String rfc;

    @Schema(description = "Número de cuenta bancaria", example = "9080512384")
    private String numeroCuenta;

    @Schema(description = "Estatus de activación", example = "true")
    private Boolean activo;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    @Schema(description = "Fecha inicial del rango de registro", example = "2026-01-01T00:00:00Z")
    private OffsetDateTime fechaInicio;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    @Schema(description = "Fecha final del rango de registro", example = "2026-12-31T23:59:59Z")
    private OffsetDateTime fechaFin;
}
