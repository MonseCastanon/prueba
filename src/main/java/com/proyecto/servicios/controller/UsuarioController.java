package com.proyecto.servicios.controller;

import com.proyecto.servicios.model.ApiResponse;
import com.proyecto.servicios.model.UsuarioActualizaRequest;
import com.proyecto.servicios.model.UsuarioBusquedaRequest;
import com.proyecto.servicios.model.UsuarioEliminaRequest;
import com.proyecto.servicios.model.UsuarioRegistroRequest;
import com.proyecto.servicios.model.UsuarioResponse;
import com.proyecto.servicios.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/usuarios")
@Tag(name = "Usuarios", description = "Administración, control de credenciales, consultas dinámicas multicriterio y ciclo de vida de usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Registrar nuevo usuario", description = "Crea una cuenta de acceso para un cliente existente asociando su credencial cifrada y rol")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Usuario registrado exitosamente"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Datos de entrada inválidos o faltantes en el payload"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Cliente asociado no encontrado"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Correo duplicado o cliente con usuario ya existente")
    })
    public ResponseEntity<ApiResponse<UsuarioResponse>> registrarUsuario(@Valid @RequestBody UsuarioRegistroRequest request) {
        UsuarioResponse usuario = usuarioService.registrarUsuario(request);
        return new ResponseEntity<>(ApiResponse.exito(HttpStatus.CREATED.value(), "Usuario registrado exitosamente", usuario), HttpStatus.CREATED);
    }

    @PostMapping(value = "/consulta", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Búsqueda dinámica de usuarios exclusivamente por RFC y CURP", description = "Permite consultar usuarios mediante los únicos dos filtros de búsqueda autorizados: RFC y CURP del cliente vinculado")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Consulta ejecutada exitosamente"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Formato de filtro inválido o cuerpo ausente"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "422", description = "No se proporcionó ningún criterio de búsqueda")
    })
    public ResponseEntity<ApiResponse<List<UsuarioResponse>>> consultarUsuarios(
            @Valid @RequestBody UsuarioBusquedaRequest request) {
        List<UsuarioResponse> resultados = usuarioService.consultarUsuarios(request);
        return ResponseEntity.ok(ApiResponse.exito("Consulta ejecutada exitosamente", resultados));
    }

    @PutMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Actualizar usuario", description = "Modifica los atributos autorizados de un usuario (correo, credencial cifrada, rol y estatus activo)")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Usuario actualizado exitosamente"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Datos de modificación inválidos"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Usuario no encontrado"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Conflicto por correo electrónico ya registrado por otro usuario")
    })
    public ResponseEntity<ApiResponse<UsuarioResponse>> actualizarUsuario(@Valid @RequestBody UsuarioActualizaRequest request) {
        UsuarioResponse actualizado = usuarioService.actualizarUsuario(request);
        return ResponseEntity.ok(ApiResponse.exito("Usuario actualizado exitosamente", actualizado));
    }

    @PostMapping(value = "/baja", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Baja lógica de usuario (POST)", description = "Inhabilita la cuenta de acceso de un usuario registrando el motivo de la operación mediante método POST")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Baja lógica aplicada exitosamente"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Datos de baja inválidos"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Usuario no encontrado"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "422", description = "Regla de negocio violada (ej. usuario ya inactivo)")
    })
    public ResponseEntity<ApiResponse<Map<String, Object>>> eliminarUsuarioPorBaja(@Valid @RequestBody UsuarioEliminaRequest request) {
        usuarioService.eliminarUsuario(request);
        Map<String, Object> respuesta = new HashMap<>();
        respuesta.put("usuarioId", request.getId());
        return ResponseEntity.ok(ApiResponse.exito("Usuario dado de baja exitosamente", respuesta));
    }
}
