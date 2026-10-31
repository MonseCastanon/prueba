package com.proyecto.servicios.controller;

import com.proyecto.servicios.config.security.AuthenticatedUser;
import com.proyecto.servicios.enums.EstadoTransaccion;
import com.proyecto.servicios.model.ApiResponse;
import com.proyecto.servicios.model.PagoServicioConsultaRequest;
import com.proyecto.servicios.model.PagoServicioRequest;
import com.proyecto.servicios.model.PagoServicioResponse;
import com.proyecto.servicios.model.PinConfiguracionRequest;
import com.proyecto.servicios.service.PagoServicioService;
import com.proyecto.servicios.service.PinService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/pagos-servicios")
@RequiredArgsConstructor
@Tag(name = "Pagos de Servicios y Recargas", description = "Operaciones financieras con PuntoRed / Gestopago")
@SecurityRequirement(name = "bearerAuth")
public class PagoServicioController {

    private final PagoServicioService pagoServicioService;
    private final PinService pinService;

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAnyRole('CLIENTE', 'ADMIN')")
    @Operation(summary = "Procesar pago de servicios o recargas electrónicas con reserva previa y validación de PIN")
    public ResponseEntity<ApiResponse<PagoServicioResponse>> pagar(
            @Valid @RequestBody PagoServicioRequest request,
            @AuthenticationPrincipal AuthenticatedUser user) {

        PagoServicioResponse response = pagoServicioService.pagar(request, user);

        if (response.estado() == EstadoTransaccion.EXITOSA) {
            return ResponseEntity.ok(ApiResponse.exito(HttpStatus.OK.value(), response.mensaje(), response));
        } else if (response.estado() == EstadoTransaccion.REVERSADA || response.estado() == EstadoTransaccion.RECHAZADA) {
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                    .body(ApiResponse.exito(HttpStatus.UNPROCESSABLE_ENTITY.value(), response.mensaje(), response));
        } else {
            return ResponseEntity.status(HttpStatus.ACCEPTED)
                    .body(ApiResponse.exito(HttpStatus.ACCEPTED.value(), response.mensaje(), response));
        }
    }

    @PostMapping(value = "/consulta", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAnyRole('CLIENTE', 'ADMIN')")
    @Operation(summary = "Consultar el estatus de una transacción financiera mediante su IdDocumento")
    public ResponseEntity<ApiResponse<PagoServicioResponse>> consultar(
            @Valid @RequestBody PagoServicioConsultaRequest request,
            @AuthenticationPrincipal AuthenticatedUser user) {

        PagoServicioResponse response = pagoServicioService.consultar(request, user);
        return ResponseEntity.ok(ApiResponse.exito(HttpStatus.OK.value(), "Consulta de transacción realizada con éxito", response));
    }

    @PutMapping(value = "/pin", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAnyRole('CLIENTE', 'ADMIN')")
    @Operation(summary = "Configurar o actualizar el PIN transaccional de 4 a 6 dígitos del usuario autenticado")
    public ResponseEntity<ApiResponse<Map<String, Object>>> configurarPin(
            @Valid @RequestBody PinConfiguracionRequest request,
            @AuthenticationPrincipal AuthenticatedUser user) {

        pinService.asignarPin(user.usuarioId(), request.pin());
        return ResponseEntity.ok(ApiResponse.exito(
                HttpStatus.OK.value(),
                "PIN transaccional configurado exitosamente",
                Map.of("usuarioId", user.usuarioId(), "pinConfigurado", true)
        ));
    }
}

