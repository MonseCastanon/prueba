package com.proyecto.servicios.controller;

import com.proyecto.servicios.enums.EstatusCuenta;
import com.proyecto.servicios.model.*;
import com.proyecto.servicios.service.CuentaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/cuentas")
@Tag(name = "Cuentas Bancarias", description = "Apertura, consultas de saldos, bloqueos, desbloqueos y patch")
public class CuentaController {

    private final CuentaService cuentaService;

    public CuentaController(CuentaService cuentaService) {
        this.cuentaService = cuentaService;
    }

    @PostMapping
    @Operation(summary = "Crear nueva cuenta asociada a un cliente existente")
    public ResponseEntity<CuentaResponse> crearCuenta(
            @RequestParam Long clienteId,
            @RequestParam(required = false) BigDecimal saldoInicial
    ) {
        return new ResponseEntity<>(cuentaService.crearCuentaParaCliente(clienteId, saldoInicial), HttpStatus.CREATED);
    }

    @GetMapping("/{numeroCuenta}")
    @Operation(summary = "Consultar cuenta por número")
    public ResponseEntity<CuentaResponse> consultarPorNumero(@PathVariable String numeroCuenta) {
        return ResponseEntity.ok(cuentaService.consultarPorNumero(numeroCuenta));
    }

    @GetMapping
    @Operation(summary = "Consultar cuentas por cliente o por estatus")
    public ResponseEntity<List<CuentaResponse>> consultarCuentas(
            @RequestParam(required = false) Long clienteId,
            @RequestParam(required = false) EstatusCuenta estatus
    ) {
        if (clienteId != null) {
            return ResponseEntity.ok(cuentaService.consultarPorClienteId(clienteId));
        }
        if (estatus != null) {
            return ResponseEntity.ok(cuentaService.consultarPorEstatus(estatus));
        }
        return ResponseEntity.ok(cuentaService.consultarCuentasActivas());
    }

    @GetMapping("/{numeroCuenta}/saldo")
    @Operation(summary = "Consultar saldo disponible")
    public ResponseEntity<Map<String, Object>> consultarSaldo(@PathVariable String numeroCuenta) {
        return ResponseEntity.ok(Map.of(
                "numeroCuenta", numeroCuenta,
                "saldo", cuentaService.consultarSaldo(numeroCuenta)
        ));
    }

    @PutMapping("/{numeroCuenta}/bloquear")
    @Operation(summary = "Bloquear cuenta bancaria por motivos de seguridad o solicitud")
    public ResponseEntity<CuentaResponse> bloquearCuenta(
            @PathVariable String numeroCuenta,
            @Valid @RequestBody BloqueoDesbloqueoCuentaRequest request
    ) {
        return ResponseEntity.ok(cuentaService.bloquearCuenta(numeroCuenta, request));
    }

    @PutMapping("/{numeroCuenta}/desbloquear")
    @Operation(summary = "Desbloquear cuenta bancaria")
    public ResponseEntity<CuentaResponse> desbloquearCuenta(
            @PathVariable String numeroCuenta,
            @Valid @RequestBody BloqueoDesbloqueoCuentaRequest request
    ) {
        return ResponseEntity.ok(cuentaService.desbloquearCuenta(numeroCuenta, request));
    }

    @PatchMapping("/{numeroCuenta}")
    @Operation(summary = "Actualización parcial de una cuenta")
    public ResponseEntity<CuentaResponse> patchCuenta(
            @PathVariable String numeroCuenta,
            @Valid @RequestBody CuentaPatchRequest request
    ) {
        return ResponseEntity.ok(cuentaService.patchCuenta(numeroCuenta, request));
    }
}
