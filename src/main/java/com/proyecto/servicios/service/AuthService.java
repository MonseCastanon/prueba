package com.proyecto.servicios.service;

import com.proyecto.servicios.config.security.JwtTokenProvider;
import com.proyecto.servicios.entity.sf.Usuario;
import com.proyecto.servicios.exception.InvalidCredentialsException;
import com.proyecto.servicios.model.LoginRequest;
import com.proyecto.servicios.model.LoginResponse;
import com.proyecto.servicios.repositorys.sf.UsuarioRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;

    public AuthService(UsuarioRepository usuarioRepository,
            PasswordEncoder passwordEncoder,
            JwtTokenProvider tokenProvider) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenProvider = tokenProvider;
    }

    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        log.info("Intento de login para usuario: {}", request.getCorreo());

        Usuario usuario = usuarioRepository.findByCorreo(request.getCorreo().toLowerCase())
                .orElseThrow(() -> new InvalidCredentialsException("Credenciales inválidas"));

        if (!Boolean.TRUE.equals(usuario.getActivo()) || (usuario.getCliente() != null && !Boolean.TRUE.equals(usuario.getCliente().getActivo()))) {
            throw new InvalidCredentialsException("Usuario inactivo en el sistema");
        }

        if (!passwordEncoder.matches(request.getPassword(), usuario.getPasswordHash())) {
            throw new InvalidCredentialsException("Credenciales inválidas");
        }

        Long clienteId = usuario.getCliente() != null ? usuario.getCliente().getId() : null;
        String token = tokenProvider.generateToken(usuario.getCorreo(), usuario.getRol(), clienteId);

        return LoginResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .expiresIn(tokenProvider.getExpirationTime())
                .correo(usuario.getCorreo())
                .rol(usuario.getRol())
                .clienteId(clienteId)
                .build();
    }
}
