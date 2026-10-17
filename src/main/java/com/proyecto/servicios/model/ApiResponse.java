package com.proyecto.servicios.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.OffsetDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Estructura uniforme estándar para todas las respuestas de la API REST")
public class ApiResponse<T> {

    @Schema(description = "Marca de tiempo de la respuesta en formato ISO-8601", example = "2026-10-16T18:30:00Z")
    private OffsetDateTime timestamp;

    @Schema(description = "Código de estado HTTP de la operación", example = "200")
    private Integer status;

    @Schema(description = "Mensaje descriptivo del resultado de la operación", example = "Operación ejecutada exitosamente")
    private String mensaje;

    @Schema(description = "Cuerpo de datos del resultado")
    private T datos;

    public static <T> ApiResponse<T> exito(Integer status, String mensaje, T datos) {
        return ApiResponse.<T>builder()
                .timestamp(OffsetDateTime.now())
                .status(status)
                .mensaje(mensaje)
                .datos(datos)
                .build();
    }

    public static <T> ApiResponse<T> exito(String mensaje, T datos) {
        return exito(200, mensaje, datos);
    }

    public static <T> ApiResponse<T> exito(String mensaje) {
        return exito(200, mensaje, null);
    }
}
