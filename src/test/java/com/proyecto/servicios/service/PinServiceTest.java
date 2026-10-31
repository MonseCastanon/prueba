package com.proyecto.servicios.service;

import com.proyecto.servicios.config.security.AuthenticatedUser;
import com.proyecto.servicios.entity.sf.Usuario;
import com.proyecto.servicios.exception.InvalidCredentialsException;
import com.proyecto.servicios.exception.PinBlockedException;
import com.proyecto.servicios.repositorys.sf.UsuarioRepository;
import com.proyecto.servicios.service.Impl.PinServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PinServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private PinServiceImpl pinService;

    private AuthenticatedUser authUser;
    private Usuario usuario;

    @BeforeEach
    void setUp() {
        authUser = new AuthenticatedUser(
                1L, 1001L, "cliente@banco.com", "hashPass", "ROLE_CLIENTE", Collections.emptyList(), true);

        usuario = Usuario.builder()
                .id(1L)
                .correo("cliente@banco.com")
                .pinHash("$2a$12$hashedPin1234")
                .pinIntentosFallidos((short) 0)
                .pinBloqueadoHasta(null)
                .activo(true)
                .build();
    }

    @Test
    @DisplayName("PIN correcto: validación exitosa y reinicio de intentos fallidos")
    void testValidarPinExitoso() {
        usuario.setPinIntentosFallidos((short) 2);
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("1234", "$2a$12$hashedPin1234")).thenReturn(true);

        assertDoesNotThrow(() -> pinService.validarPin(authUser, "1234"));
        assertEquals((short) 0, usuario.getPinIntentosFallidos());
        verify(usuarioRepository, times(1)).save(usuario);
    }

    @Test
    @DisplayName("PIN incorrecto: incrementa contador de intentos y lanza InvalidCredentialsException")
    void testValidarPinIncorrecto() {
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("9999", "$2a$12$hashedPin1234")).thenReturn(false);

        assertThrows(InvalidCredentialsException.class, () -> pinService.validarPin(authUser, "9999"));
        assertEquals((short) 1, usuario.getPinIntentosFallidos());
        verify(usuarioRepository, times(1)).save(usuario);
    }

    @Test
    @DisplayName("Tercer intento fallido de PIN: bloquea el PIN y lanza PinBlockedException (HTTP 423)")
    void testBloqueoPinAlTercerFallo() {
        usuario.setPinIntentosFallidos((short) 2);
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("0000", "$2a$12$hashedPin1234")).thenReturn(false);

        assertThrows(PinBlockedException.class, () -> pinService.validarPin(authUser, "0000"));
        assertEquals((short) 3, usuario.getPinIntentosFallidos());
        assertNotNull(usuario.getPinBloqueadoHasta());
        assertTrue(usuario.getPinBloqueadoHasta().isAfter(OffsetDateTime.now()));
        verify(usuarioRepository, times(1)).save(usuario);
    }

    @Test
    @DisplayName("Intento con PIN temporalmente bloqueado lanza PinBlockedException inmediatamente")
    void testPinYaBloqueado() {
        usuario.setPinBloqueadoHasta(OffsetDateTime.now().plusMinutes(10));
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));

        assertThrows(PinBlockedException.class, () -> pinService.validarPin(authUser, "1234"));
        verify(passwordEncoder, never()).matches(anyString(), anyString());
    }

    @Test
    @DisplayName("Usuario sin PIN configurado lanza InvalidCredentialsException")
    void testUsuarioSinPin() {
        usuario.setPinHash(null);
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));

        assertThrows(InvalidCredentialsException.class, () -> pinService.validarPin(authUser, "1234"));
    }

    @Test
    @DisplayName("Asignación de nuevo PIN: valida formato 4-6 dígitos y codifica con BCrypt")
    void testAsignarPinExitoso() {
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(passwordEncoder.encode("5678")).thenReturn("$2a$12$newEncodedPin");

        pinService.asignarPin(1L, "5678");

        assertEquals("$2a$12$newEncodedPin", usuario.getPinHash());
        assertEquals((short) 0, usuario.getPinIntentosFallidos());
        assertNull(usuario.getPinBloqueadoHasta());
        verify(usuarioRepository, times(1)).save(usuario);
    }

    @Test
    @DisplayName("Asignación de PIN rechaza formatos inválidos")
    void testAsignarPinFormatoInvalido() {
        assertThrows(IllegalArgumentException.class, () -> pinService.asignarPin(1L, "12")); // < 4 dígitos
        assertThrows(IllegalArgumentException.class, () -> pinService.asignarPin(1L, "1234567")); // > 6 dígitos
        assertThrows(IllegalArgumentException.class, () -> pinService.asignarPin(1L, "abcd")); // No numérico
    }
}

