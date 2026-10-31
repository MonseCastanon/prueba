package com.proyecto.servicios.service.Impl;

import com.proyecto.servicios.config.security.AuthenticatedUser;
import com.proyecto.servicios.entity.sf.Usuario;
import com.proyecto.servicios.exception.InvalidCredentialsException;
import com.proyecto.servicios.exception.PinBlockedException;
import com.proyecto.servicios.exception.ResourceNotFoundException;
import com.proyecto.servicios.repositorys.sf.UsuarioRepository;
import com.proyecto.servicios.service.PinService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class PinServiceImpl implements PinService {

    private static final int MAX_INTENTOS_PIN = 3;
    private static final int MINUTOS_BLOQUEO = 15;

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void validarPin(AuthenticatedUser user, String pin) {
        if (user == null || user.usuarioId() == null) {
            throw new InvalidCredentialsException("Usuario no autenticado");
        }

        if (pin == null || pin.isBlank()) {
            throw new InvalidCredentialsException("El PIN transaccional es obligatorio");
        }

        Usuario usuario = usuarioRepository.findById(user.usuarioId())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        OffsetDateTime ahora = OffsetDateTime.now();

        if (usuario.getPinBloqueadoHasta() != null) {
            if (ahora.isBefore(usuario.getPinBloqueadoHasta())) {
                log.warn("Intento de uso de PIN bloqueado para usuarioId={}", usuario.getId());
                throw new PinBlockedException("El PIN transaccional se encuentra temporalmente bloqueado por exceso de intentos fallidos. Intente más tarde.");
            } else {
                // El bloqueo ya expiró, restablecer
                usuario.setPinBloqueadoHasta(null);
                usuario.setPinIntentosFallidos((short) 0);
            }
        }

        if (usuario.getPinHash() == null || usuario.getPinHash().isBlank()) {
            log.warn("Usuario {} no tiene PIN configurado", usuario.getId());
            throw new InvalidCredentialsException("PIN transaccional no configurado o inválido");
        }

        if (!passwordEncoder.matches(pin, usuario.getPinHash())) {
            short fallidos = (short) (usuario.getPinIntentosFallidos() + 1);
            usuario.setPinIntentosFallidos(fallidos);

            if (fallidos >= MAX_INTENTOS_PIN) {
                usuario.setPinBloqueadoHasta(ahora.plusMinutes(MINUTOS_BLOQUEO));
                usuarioRepository.save(usuario);
                log.warn("PIN bloqueado tras {} intentos fallidos para usuarioId={}", fallidos, usuario.getId());
                throw new PinBlockedException("El PIN transaccional ha sido bloqueado temporalmente por superar el límite de intentos permitidos.");
            }

            usuarioRepository.save(usuario);
            log.warn("PIN incorrecto para usuarioId={}, intento {} de {}", usuario.getId(), fallidos, MAX_INTENTOS_PIN);
            throw new InvalidCredentialsException("PIN transaccional incorrecto");
        }

        // PIN correcto: restablecer contador si era mayor a cero
        if (usuario.getPinIntentosFallidos() > 0 || usuario.getPinBloqueadoHasta() != null) {
            usuario.setPinIntentosFallidos((short) 0);
            usuario.setPinBloqueadoHasta(null);
            usuarioRepository.save(usuario);
        }
    }

    @Override
    @Transactional
    public void asignarPin(Long usuarioId, String pin) {
        if (pin == null || !pin.matches("\\d{4,6}")) {
            throw new IllegalArgumentException("El PIN debe constar de 4 a 6 dígitos numéricos");
        }

        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con ID: " + usuarioId));

        usuario.setPinHash(passwordEncoder.encode(pin));
        usuario.setPinIntentosFallidos((short) 0);
        usuario.setPinBloqueadoHasta(null);
        usuarioRepository.save(usuario);
        log.info("PIN transaccional asignado exitosamente para usuarioId={}", usuarioId);
    }
}

