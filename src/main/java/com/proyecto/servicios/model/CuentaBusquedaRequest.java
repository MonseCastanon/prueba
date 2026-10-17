package com.proyecto.servicios.model;

import com.proyecto.servicios.enums.EstatusCuenta;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Criterios opcionales para la consulta dinámica de cuentas bancarias vía RequestBody")
public class CuentaBusquedaRequest {

    @Schema(description = "Número de cuenta bancaria de 10 dígitos", example = "9080512384")
    private String numeroCuenta;

    @Schema(description = "Identificador del cliente titular", example = "1001")
    private Long clienteId;

    @Schema(description = "Estatus de la cuenta bancaria", example = "ACTIVA")
    private EstatusCuenta estatus;
}
