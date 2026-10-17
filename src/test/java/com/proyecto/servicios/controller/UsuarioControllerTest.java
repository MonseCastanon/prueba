package com.proyecto.servicios.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.proyecto.servicios.exception.GlobalExceptionHandler;
import com.proyecto.servicios.model.*;
import com.proyecto.servicios.service.UsuarioService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.OffsetDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class UsuarioControllerTest {

    private MockMvc mockMvc;

    private ObjectMapper objectMapper;

    @Mock
    private UsuarioService usuarioService;

    @InjectMocks
    private UsuarioController usuarioController;

    private UsuarioResponse responseMock;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(usuarioController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        responseMock = UsuarioResponse.builder()
                .id(1L)
                .correo("admin@empresa.com")
                .rol("ROLE_ADMIN")
                .activo(true)
                .clienteId(1001L)
                .fechaCreacion(OffsetDateTime.now())
                .fechaActualizacion(OffsetDateTime.now())
                .build();
    }

    @Test
    @DisplayName("POST /usuarios - Registra usuario exitosamente y retorna 201 CREATED con ApiResponse")
    void testRegistrarUsuarioExitoso() throws Exception {
        UsuarioRegistroRequest request = UsuarioRegistroRequest.builder()
                .correo("admin@empresa.com")
                .password("AdminSecure2026!")
                .rol("ROLE_ADMIN")
                .clienteId(1001L)
                .build();

        when(usuarioService.registrarUsuario(any(UsuarioRegistroRequest.class))).thenReturn(responseMock);

        mockMvc.perform(post("/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value(201))
                .andExpect(jsonPath("$.mensaje").value("Usuario registrado exitosamente"))
                .andExpect(jsonPath("$.datos.id").value(1L))
                .andExpect(jsonPath("$.datos.correo").value("admin@empresa.com"))
                .andExpect(jsonPath("$.datos.rol").value("ROLE_ADMIN"))
                .andExpect(jsonPath("$.datos.activo").value(true))
                .andExpect(jsonPath("$.datos.clienteId").value(1001L));

        verify(usuarioService, times(1)).registrarUsuario(any(UsuarioRegistroRequest.class));
    }

    @Test
    @DisplayName("POST /usuarios - Retorna 400 BAD REQUEST ante contraseña débil o correo inválido")
    void testRegistrarUsuarioValidacionInvalida() throws Exception {
        UsuarioRegistroRequest requestInvalido = UsuarioRegistroRequest.builder()
                .correo("formato-correo-invalido")
                .password("simple") // No cumple longitud ni caracteres especiales
                .rol("ROL_INVALIDO") // No cumple regex
                .clienteId(null) // Requerido
                .build();

        mockMvc.perform(post("/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestInvalido)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.detalles").isMap());

        verify(usuarioService, never()).registrarUsuario(any());
    }

    @Test
    @DisplayName("POST /usuarios/consulta - Retorna 200 OK con resultados de búsqueda por RFC y CURP en ApiResponse")
    void testConsultarUsuarios() throws Exception {
        UsuarioBusquedaRequest filtro = UsuarioBusquedaRequest.builder()
                .rfc("PELJ950815XXX")
                .curp("PELJ950815HDFRPR01")
                .build();

        when(usuarioService.consultarUsuarios(any())).thenReturn(List.of(responseMock));

        mockMvc.perform(post("/usuarios/consulta")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(filtro)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.datos[0].id").value(1L))
                .andExpect(jsonPath("$.datos[0].correo").value("admin@empresa.com"))
                .andExpect(jsonPath("$.datos[0].rol").value("ROLE_ADMIN"));

        verify(usuarioService, times(1)).consultarUsuarios(any());
    }

    @Test
    @DisplayName("POST /usuarios/consulta - Retorna 400 BAD REQUEST si ambos filtros (RFC y CURP) están ausentes")
    void testConsultarUsuariosSinFiltros() throws Exception {
        UsuarioBusquedaRequest filtroVacio = UsuarioBusquedaRequest.builder().build();

        mockMvc.perform(post("/usuarios/consulta")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(filtroVacio)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));

        verify(usuarioService, never()).consultarUsuarios(any());
    }

    @Test
    @DisplayName("PUT /usuarios - Modifica usuario y retorna 200 OK en ApiResponse")
    void testActualizarUsuario() throws Exception {
        UsuarioActualizaRequest request = UsuarioActualizaRequest.builder()
                .id(1L)
                .correo("admin_nuevo@empresa.com")
                .rol("ROLE_ADMIN")
                .activo(true)
                .build();

        when(usuarioService.actualizarUsuario(any(UsuarioActualizaRequest.class))).thenReturn(responseMock);

        mockMvc.perform(put("/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.datos.id").value(1L));

        verify(usuarioService, times(1)).actualizarUsuario(any(UsuarioActualizaRequest.class));
    }

    @Test
    @DisplayName("POST /usuarios/baja - Aplica baja lógica con motivo y retorna 200 OK en ApiResponse")
    void testEliminarUsuarioPorBaja() throws Exception {
        UsuarioEliminaRequest request = UsuarioEliminaRequest.builder()
                .id(1L)
                .motivo("Reestructuración de personal operativo")
                .build();

        doNothing().when(usuarioService).eliminarUsuario(any(UsuarioEliminaRequest.class));

        mockMvc.perform(post("/usuarios/baja")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.mensaje").value("Usuario dado de baja exitosamente"))
                .andExpect(jsonPath("$.datos.usuarioId").value(1L));

        verify(usuarioService, times(1)).eliminarUsuario(any(UsuarioEliminaRequest.class));
    }

    @Test
    @DisplayName("DELETE /usuarios - El método HTTP DELETE no está permitido en la API")
    void testEliminarUsuarioPorDeleteNoPermitido() throws Exception {
        UsuarioEliminaRequest request = UsuarioEliminaRequest.builder()
                .id(1L)
                .motivo("Solicitud expresa del titular")
                .build();

        mockMvc.perform(delete("/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isMethodNotAllowed());

        verify(usuarioService, never()).eliminarUsuario(any(UsuarioEliminaRequest.class));
    }
}
