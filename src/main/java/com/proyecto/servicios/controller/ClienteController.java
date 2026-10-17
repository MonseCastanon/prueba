package com.proyecto.servicios.controller;

import com.proyecto.servicios.model.*;
import com.proyecto.servicios.service.ClienteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/clientes")
@Tag(name = "Clientes", description = "Onboarding bancario, búsquedas multicriterio, actualización y baja lógica sin PathVariables ni RequestParams")
public class ClienteController {

    private final ClienteService clienteService;

    public ClienteController(ClienteService clienteService) {
        this.clienteService = clienteService;
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Registrar nuevo cliente físico (Onboarding completo con cuenta y usuario)")
    public ResponseEntity<ApiResponse<ClienteResponse>> registrarCliente(@Valid @RequestBody ClienteRegistroRequest request) {
        ClienteResponse cliente = clienteService.registrarCliente(request);
        return new ResponseEntity<>(ApiResponse.exito(HttpStatus.CREATED.value(), "Cliente registrado exitosamente", cliente), HttpStatus.CREATED);
    }

    @PostMapping(value = "/consulta", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Consultar clientes con filtros multicriterio dinámicos vía RequestBody (Nombre, Apellidos, CURP, RFC, Cuenta, Fechas)")
    public ResponseEntity<ApiResponse<List<ClienteResponse>>> consultarClientes(@Valid @RequestBody(required = false) ClienteBusquedaRequest request) {
        if (request == null) {
            return ResponseEntity.ok(ApiResponse.exito("Consulta ejecutada exitosamente", clienteService.consultarTodos()));
        }

        if (request.getCurp() != null && !request.getCurp().isBlank()) {
            return ResponseEntity.ok(ApiResponse.exito("Cliente consultado por CURP",
                    List.of(clienteService.consultarPorCurp(request.getCurp()))));
        }

        if (request.getRfc() != null && !request.getRfc().isBlank()) {
            return ResponseEntity.ok(ApiResponse.exito("Cliente consultado por RFC",
                    List.of(clienteService.consultarPorRfc(request.getRfc()))));
        }

        if (request.getNumeroCuenta() != null && !request.getNumeroCuenta().isBlank()) {
            return ResponseEntity.ok(ApiResponse.exito("Cliente consultado por número de cuenta",
                    List.of(clienteService.consultarPorNumeroCuenta(request.getNumeroCuenta()))));
        }

        if (request.getFechaInicio() != null && request.getFechaFin() != null) {
            return ResponseEntity.ok(ApiResponse.exito("Clientes consultados por rango de fechas",
                    clienteService.buscarPorRangoFechas(request.getFechaInicio(), request.getFechaFin())));
        }

        if (request.getNombre() != null || request.getApellidoPaterno() != null
                || request.getApellidoMaterno() != null || request.getActivo() != null) {
            return ResponseEntity.ok(ApiResponse.exito("Clientes consultados por filtros multicriterio",
                    clienteService.buscarPorFiltros(request.getNombre(), request.getApellidoPaterno(),
                            request.getApellidoMaterno(), request.getCurp(), request.getActivo())));
        }

        return ResponseEntity.ok(ApiResponse.exito("Consulta ejecutada exitosamente", clienteService.consultarTodos()));
    }

    @PutMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Actualización completa (PUT) de datos permitidos recibiendo el ID en el body")
    public ResponseEntity<ApiResponse<ClienteResponse>> actualizarCliente(@Valid @RequestBody ClienteActualizaRequest request) {
        ClienteResponse actualizado = clienteService.actualizarCliente(request.getId(), request);
        return ResponseEntity.ok(ApiResponse.exito("Cliente actualizado exitosamente", actualizado));
    }

    @PatchMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Actualización parcial (PATCH) de datos permitidos recibiendo el ID en el body")
    public ResponseEntity<ApiResponse<ClienteResponse>> patchCliente(@Valid @RequestBody ClientePatchRequest request) {
        ClienteResponse actualizado = clienteService.patchCliente(request.getId(), request);
        return ResponseEntity.ok(ApiResponse.exito("Cliente actualizado parcialmente con éxito", actualizado));
    }

    @PostMapping(value = "/baja", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Baja lógica de cliente (POST) (desactiva cliente, usuario y cancela cuentas) vía RequestBody")
    public ResponseEntity<ApiResponse<GenericResponse>> bajaLogica(@Valid @RequestBody ClienteBajaRequest request) {
        GenericResponse respuesta = clienteService.bajaLogicaCliente(request.getId());
        return ResponseEntity.ok(ApiResponse.exito("Baja lógica de cliente procesada exitosamente", respuesta));
    }
}
