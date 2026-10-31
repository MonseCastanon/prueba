package com.proyecto.servicios.controller;

import com.proyecto.servicios.config.security.AuthenticatedUser;
import com.proyecto.servicios.enums.EstatusCuenta;
import com.proyecto.servicios.exception.ResourceNotFoundException;
import com.proyecto.servicios.model.*;
import com.proyecto.servicios.service.CuentaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/cuentas")
@Tag(name = "Cuentas Bancarias", description = "Apertura, consultas de saldos, bloqueos, desbloqueos y patch sin PathVariables ni RequestParams")
public class CuentaController {

    private final CuentaService cuentaService;

    public CuentaController(CuentaService cuentaService) {
        this.cuentaService = cuentaService;
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Crear nueva cuenta asociada a un cliente existente vía RequestBody")
    public ResponseEntity<ApiResponse<CuentaResponse>> crearCuenta(
            @Valid @RequestBody CuentaCreacionRequest request,
            @AuthenticationPrincipal AuthenticatedUser user) {
        validarPropiedadCliente(user, request.getClienteId());
        CuentaResponse cuenta = cuentaService.crearCuentaParaCliente(request.getClienteId(), request.getSaldoInicial());
        return new ResponseEntity<>(ApiResponse.exito(HttpStatus.CREATED.value(), "Cuenta creada exitosamente", cuenta), HttpStatus.CREATED);
    }

    @PostMapping(value = "/consulta", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Consultar cuentas por cliente, por número o por estatus vía RequestBody")
    public ResponseEntity<ApiResponse<List<CuentaResponse>>> consultarCuentas(
            @Valid @RequestBody(required = false) CuentaBusquedaRequest request,
            @AuthenticationPrincipal AuthenticatedUser user) {
        if (esCliente(user)) {
            if (request != null && request.getNumeroCuenta() != null && !request.getNumeroCuenta().isBlank()) {
                CuentaResponse cta = validarPropiedadCuenta(user, request.getNumeroCuenta());
                return ResponseEntity.ok(ApiResponse.exito("Cuenta consultada por número", List.of(cta)));
            }
            return ResponseEntity.ok(ApiResponse.exito("Cuentas consultadas por cliente",
                    cuentaService.consultarPorClienteId(user.clienteId())));
        }

        if (request == null) {
            return ResponseEntity.ok(ApiResponse.exito("Cuentas activas consultadas exitosamente",
                    cuentaService.consultarCuentasActivas()));
        }

        if (request.getNumeroCuenta() != null && !request.getNumeroCuenta().isBlank()) {
            return ResponseEntity.ok(ApiResponse.exito("Cuenta consultada por número",
                    List.of(cuentaService.consultarPorNumero(request.getNumeroCuenta()))));
        }

        if (request.getClienteId() != null) {
            return ResponseEntity.ok(ApiResponse.exito("Cuentas consultadas por cliente",
                    cuentaService.consultarPorClienteId(request.getClienteId())));
        }

        if (request.getEstatus() != null) {
            return ResponseEntity.ok(ApiResponse.exito("Cuentas consultadas por estatus",
                    cuentaService.consultarPorEstatus(request.getEstatus())));
        }

        return ResponseEntity.ok(ApiResponse.exito("Cuentas activas consultadas exitosamente",
                cuentaService.consultarCuentasActivas()));
    }

    @PostMapping(value = "/saldo", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Consultar saldo disponible vía RequestBody")
    public ResponseEntity<ApiResponse<Map<String, Object>>> consultarSaldo(
            @Valid @RequestBody CuentaSaldoRequest request,
            @AuthenticationPrincipal AuthenticatedUser user) {
        validarPropiedadCuenta(user, request.getNumeroCuenta());
        Map<String, Object> data = Map.of(
                "numeroCuenta", request.getNumeroCuenta(),
                "saldo", cuentaService.consultarSaldo(request.getNumeroCuenta())
        );
        return ResponseEntity.ok(ApiResponse.exito("Saldo consultado exitosamente", data));
    }

    @PutMapping(value = "/bloquear", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Bloquear cuenta bancaria por motivos de seguridad o solicitud vía RequestBody")
    public ResponseEntity<ApiResponse<CuentaResponse>> bloquearCuenta(
            @Valid @RequestBody BloqueoDesbloqueoCuentaRequest request,
            @AuthenticationPrincipal AuthenticatedUser user) {
        validarPropiedadCuenta(user, request.getNumeroCuenta());
        CuentaResponse cuenta = cuentaService.bloquearCuenta(request.getNumeroCuenta(), request);
        return ResponseEntity.ok(ApiResponse.exito("Cuenta bloqueada exitosamente", cuenta));
    }

    @PutMapping(value = "/desbloquear", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Desbloquear cuenta bancaria vía RequestBody")
    public ResponseEntity<ApiResponse<CuentaResponse>> desbloquearCuenta(
            @Valid @RequestBody BloqueoDesbloqueoCuentaRequest request,
            @AuthenticationPrincipal AuthenticatedUser user) {
        validarPropiedadCuenta(user, request.getNumeroCuenta());
        CuentaResponse cuenta = cuentaService.desbloquearCuenta(request.getNumeroCuenta(), request);
        return ResponseEntity.ok(ApiResponse.exito("Cuenta desbloqueada exitosamente", cuenta));
    }

    @PatchMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Actualización parcial de una cuenta vía RequestBody")
    public ResponseEntity<ApiResponse<CuentaResponse>> patchCuenta(
            @Valid @RequestBody CuentaPatchRequest request,
            @AuthenticationPrincipal AuthenticatedUser user) {
        validarPropiedadCuenta(user, request.getNumeroCuenta());
        CuentaResponse cuenta = cuentaService.patchCuenta(request.getNumeroCuenta(), request);
        return ResponseEntity.ok(ApiResponse.exito("Cuenta actualizada parcialmente con éxito", cuenta));
    }

    private boolean esCliente(AuthenticatedUser user) {
        return user != null && "ROLE_CLIENTE".equalsIgnoreCase(user.rol()) && user.clienteId() != null;
    }

    private void validarPropiedadCliente(AuthenticatedUser user, Long clienteIdSolicitado) {
        if (esCliente(user) && !user.clienteId().equals(clienteIdSolicitado)) {
            throw new ResourceNotFoundException("Cliente no encontrado con ID: " + clienteIdSolicitado);
        }
    }

    private CuentaResponse validarPropiedadCuenta(AuthenticatedUser user, String numeroCuenta) {
        CuentaResponse cta = cuentaService.consultarPorNumero(numeroCuenta);
        if (esCliente(user) && !user.clienteId().equals(cta.getClienteId())) {
            throw new ResourceNotFoundException("Cuenta bancaria no encontrada con número: " + numeroCuenta);
        }
        return cta;
    }
}
