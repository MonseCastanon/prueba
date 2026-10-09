package com.proyecto.servicios.controller;

import com.proyecto.servicios.model.*;
import com.proyecto.servicios.service.ClienteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.util.List;

@RestController
@RequestMapping("/clientes")
@Tag(name = "Clientes", description = "Onboarding bancario, búsquedas multicriterio, actualización y baja lógica")
public class ClienteController {

    private final ClienteService clienteService;

    public ClienteController(ClienteService clienteService) {
        this.clienteService = clienteService;
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Registrar nuevo cliente físico (Onboarding completo con cuenta y usuario)")
    public ResponseEntity<ClienteResponse> registrarCliente(@Valid @RequestBody ClienteRegistroRequest request) {
        return new ResponseEntity<>(clienteService.registrarCliente(request), HttpStatus.CREATED);
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Consultar clientes con filtros multicriterio (Nombre, Apellido Paterno, Apellido Materno, CURP, Activo)")
    public ResponseEntity<List<ClienteResponse>> consultarClientes(
            @RequestParam(required = false) String nombre,
            @RequestParam(required = false) String apellidoPaterno,
            @RequestParam(required = false) String apellidoMaterno,
            @RequestParam(required = false) String curp,
            @RequestParam(required = false) Boolean activo
    ) {
        if (nombre != null || apellidoPaterno != null || apellidoMaterno != null || curp != null || activo != null) {
            return ResponseEntity.ok(clienteService.buscarPorFiltros(nombre, apellidoPaterno, apellidoMaterno, curp, activo));
        }
        return ResponseEntity.ok(clienteService.consultarTodos());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consultar cliente por ID")
    public ResponseEntity<ClienteResponse> consultarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(clienteService.consultarPorId(id));
    }

    @GetMapping("/curp/{curp}")
    @Operation(summary = "Consultar cliente por CURP")
    public ResponseEntity<ClienteResponse> consultarPorCurp(@PathVariable String curp) {
        return ResponseEntity.ok(clienteService.consultarPorCurp(curp));
    }

    @GetMapping("/rfc/{rfc}")
    @Operation(summary = "Consultar cliente por RFC")
    public ResponseEntity<ClienteResponse> consultarPorRfc(@PathVariable String rfc) {
        return ResponseEntity.ok(clienteService.consultarPorRfc(rfc));
    }

    @GetMapping("/cuenta/{numeroCuenta}")
    @Operation(summary = "Consultar cliente titular por número de cuenta bancaria")
    public ResponseEntity<ClienteResponse> consultarPorNumeroCuenta(@PathVariable String numeroCuenta) {
        return ResponseEntity.ok(clienteService.consultarPorNumeroCuenta(numeroCuenta));
    }

    @GetMapping("/rango-fechas")
    @Operation(summary = "Consultar clientes creados en un rango de fechas")
    public ResponseEntity<List<ClienteResponse>> consultarPorFechas(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime fechaInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime fechaFin
    ) {
        return ResponseEntity.ok(clienteService.buscarPorRangoFechas(fechaInicio, fechaFin));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualización completa (PUT) de datos permitidos")
    public ResponseEntity<ClienteResponse> actualizarCliente(
            @PathVariable Long id,
            @Valid @RequestBody ClienteActualizaRequest request
    ) {
        return ResponseEntity.ok(clienteService.actualizarCliente(id, request));
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Actualización parcial (PATCH) de datos permitidos")
    public ResponseEntity<ClienteResponse> patchCliente(
            @PathVariable Long id,
            @Valid @RequestBody ClientePatchRequest request
    ) {
        return ResponseEntity.ok(clienteService.patchCliente(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Baja lógica de cliente (desactiva cliente, usuario y cancela cuentas)")
    public ResponseEntity<GenericResponse> bajaLogica(@PathVariable Long id) {
        return ResponseEntity.ok(clienteService.bajaLogicaCliente(id));
    }
}
