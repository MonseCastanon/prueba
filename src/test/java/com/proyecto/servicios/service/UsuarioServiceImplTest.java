package com.proyecto.servicios.service;

import com.proyecto.servicios.entity.sf.Cliente;
import com.proyecto.servicios.entity.sf.Usuario;
import com.proyecto.servicios.exception.BusinessRuleException;
import com.proyecto.servicios.exception.DuplicateResourceException;
import com.proyecto.servicios.exception.ResourceNotFoundException;
import com.proyecto.servicios.model.*;
import com.proyecto.servicios.repositorys.sf.ClienteRepository;
import com.proyecto.servicios.repositorys.sf.UsuarioRepository;
import com.proyecto.servicios.service.Impl.UsuarioServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceImplTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private ClienteRepository clienteRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UsuarioServiceImpl usuarioService;

    private Cliente clienteMock;
    private Usuario usuarioMock;

    @BeforeEach
    void setUp() {
        clienteMock = Cliente.builder()
                .id(1001L)
                .nombre("Juan")
                .apellidoPaterno("Pérez")
                .apellidoMaterno("López")
                .correo("juan.perez@empresa.com")
                .activo(true)
                .build();

        usuarioMock = Usuario.builder()
                .id(1L)
                .correo("juan.perez@empresa.com")
                .passwordHash("$2a$12$e0MYHASHEDPASSWORDVALUE12345678901234567890")
                .rol("ROLE_CLIENTE")
                .activo(true)
                .cliente(clienteMock)
                .fechaCreacion(OffsetDateTime.now())
                .fechaActualizacion(OffsetDateTime.now())
                .build();
    }

    @Test
    @DisplayName("registrarUsuario: Éxito al registrar usuario con cliente existente y contraseña cifrada")
    void testRegistrarUsuarioExitoso() {
        UsuarioRegistroRequest request = UsuarioRegistroRequest.builder()
                .correo("juan.perez@empresa.com")
                .password("Password123!")
                .rol("ROLE_CLIENTE")
                .clienteId(1001L)
                .build();

        when(usuarioRepository.existsByCorreo("juan.perez@empresa.com")).thenReturn(false);
        when(clienteRepository.findById(1001L)).thenReturn(Optional.of(clienteMock));
        when(usuarioRepository.existsByClienteId(1001L)).thenReturn(false);
        when(passwordEncoder.encode("Password123!")).thenReturn("hashedPassword123");
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuarioMock);

        UsuarioResponse response = usuarioService.registrarUsuario(request);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("juan.perez@empresa.com", response.getCorreo());
        assertEquals("ROLE_CLIENTE", response.getRol());
        assertEquals(1001L, response.getClienteId());
        assertTrue(response.getActivo());

        verify(usuarioRepository, times(1)).save(any(Usuario.class));
        verify(passwordEncoder, times(1)).encode("Password123!");
    }

    @Test
    @DisplayName("registrarUsuario: Falla con DuplicateResourceException si el correo ya existe")
    void testRegistrarUsuarioCorreoDuplicado() {
        UsuarioRegistroRequest request = UsuarioRegistroRequest.builder()
                .correo("juan.perez@empresa.com")
                .password("Password123!")
                .rol("ROLE_CLIENTE")
                .clienteId(1001L)
                .build();

        when(usuarioRepository.existsByCorreo("juan.perez@empresa.com")).thenReturn(true);

        DuplicateResourceException ex = assertThrows(DuplicateResourceException.class,
                () -> usuarioService.registrarUsuario(request));

        assertTrue(ex.getMessage().contains("ya se encuentra registrado"));
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    @DisplayName("registrarUsuario: Falla con ResourceNotFoundException si el cliente no existe")
    void testRegistrarUsuarioClienteNoEncontrado() {
        UsuarioRegistroRequest request = UsuarioRegistroRequest.builder()
                .correo("juan.perez@empresa.com")
                .password("Password123!")
                .rol("ROLE_CLIENTE")
                .clienteId(9999L)
                .build();

        when(usuarioRepository.existsByCorreo("juan.perez@empresa.com")).thenReturn(false);
        when(clienteRepository.findById(9999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> usuarioService.registrarUsuario(request));

        verify(usuarioRepository, never()).save(any());
    }

    @Test
    @DisplayName("registrarUsuario: Falla con DuplicateResourceException si el cliente ya tiene usuario")
    void testRegistrarUsuarioClienteYaTieneUsuario() {
        UsuarioRegistroRequest request = UsuarioRegistroRequest.builder()
                .correo("juan.perez@empresa.com")
                .password("Password123!")
                .rol("ROLE_CLIENTE")
                .clienteId(1001L)
                .build();

        when(usuarioRepository.existsByCorreo("juan.perez@empresa.com")).thenReturn(false);
        when(clienteRepository.findById(1001L)).thenReturn(Optional.of(clienteMock));
        when(usuarioRepository.existsByClienteId(1001L)).thenReturn(true);

        assertThrows(DuplicateResourceException.class,
                () -> usuarioService.registrarUsuario(request));

        verify(usuarioRepository, never()).save(any());
    }

    @Test
    @DisplayName("consultarUsuarios: Retorna lista de usuarios según filtros exclusivos RFC y CURP")
    void testConsultarUsuariosConFiltro() {
        UsuarioBusquedaRequest filtro = UsuarioBusquedaRequest.builder()
                .rfc("PELJ950815XXX")
                .curp("PELJ950815HDFRPR01")
                .build();

        when(usuarioRepository.findAll(any(Specification.class))).thenReturn(List.of(usuarioMock));

        List<UsuarioResponse> resultados = usuarioService.consultarUsuarios(filtro);

        assertNotNull(resultados);
        assertEquals(1, resultados.size());
        assertEquals("juan.perez@empresa.com", resultados.get(0).getCorreo());
        verify(usuarioRepository, times(1)).findAll(any(Specification.class));
    }

    @Test
    @DisplayName("consultarUsuarios: Retorna lista vacía si no hay coincidencias con filtro válido")
    void testConsultarUsuariosSinResultados() {
        UsuarioBusquedaRequest filtro = UsuarioBusquedaRequest.builder()
                .rfc("XAXX010101000")
                .build();
        when(usuarioRepository.findAll(any(Specification.class))).thenReturn(Collections.emptyList());

        List<UsuarioResponse> resultados = usuarioService.consultarUsuarios(filtro);

        assertNotNull(resultados);
        assertTrue(resultados.isEmpty());
    }

    @Test
    @DisplayName("consultarUsuarios: Falla con BusinessRuleException si no se envía ningún filtro")
    void testConsultarUsuariosSinFiltrosFalla() {
        assertThrows(BusinessRuleException.class, () -> usuarioService.consultarUsuarios(null));

        UsuarioBusquedaRequest filtroVacio = UsuarioBusquedaRequest.builder().build();
        assertThrows(BusinessRuleException.class, () -> usuarioService.consultarUsuarios(filtroVacio));
    }

    @Test
    @DisplayName("actualizarUsuario: Éxito al actualizar correo, rol y password con rehasheo")
    void testActualizarUsuarioExitoso() {
        UsuarioActualizaRequest request = UsuarioActualizaRequest.builder()
                .id(1L)
                .correo("nuevo.correo@empresa.com")
                .password("NewPassword2026!")
                .rol("ROLE_OPERADOR")
                .activo(true)
                .build();

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuarioMock));
        when(usuarioRepository.existsByCorreo("nuevo.correo@empresa.com")).thenReturn(false);
        when(passwordEncoder.encode("NewPassword2026!")).thenReturn("newHashedPassword");
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuarioMock);

        UsuarioResponse response = usuarioService.actualizarUsuario(request);

        assertNotNull(response);
        verify(passwordEncoder, times(1)).encode("NewPassword2026!");
        verify(usuarioRepository, times(1)).save(usuarioMock);
    }

    @Test
    @DisplayName("actualizarUsuario: Falla con DuplicateResourceException si el nuevo correo pertenece a otro usuario")
    void testActualizarUsuarioCorreoDuplicado() {
        UsuarioActualizaRequest request = UsuarioActualizaRequest.builder()
                .id(1L)
                .correo("otro.usuario@empresa.com")
                .build();

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuarioMock));
        when(usuarioRepository.existsByCorreo("otro.usuario@empresa.com")).thenReturn(true);

        assertThrows(DuplicateResourceException.class,
                () -> usuarioService.actualizarUsuario(request));

        verify(usuarioRepository, never()).save(any());
    }

    @Test
    @DisplayName("eliminarUsuario: Aplica baja lógica cambiando estatus activo a false")
    void testEliminarUsuarioBajaLogica() {
        UsuarioEliminaRequest request = UsuarioEliminaRequest.builder()
                .id(1L)
                .motivo("Cancelación voluntaria de servicios bancarios")
                .build();

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuarioMock));
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuarioMock);

        usuarioService.eliminarUsuario(request);

        assertFalse(usuarioMock.getActivo());
        verify(usuarioRepository, times(1)).save(usuarioMock);
    }

    @Test
    @DisplayName("eliminarUsuario: Falla con BusinessRuleException si el usuario ya está inactivo")
    void testEliminarUsuarioYaInactivo() {
        usuarioMock.setActivo(false);
        UsuarioEliminaRequest request = UsuarioEliminaRequest.builder()
                .id(1L)
                .motivo("Baja repetida")
                .build();

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuarioMock));

        assertThrows(BusinessRuleException.class,
                () -> usuarioService.eliminarUsuario(request));

        verify(usuarioRepository, never()).save(any());
    }
}
