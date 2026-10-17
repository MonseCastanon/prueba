package com.proyecto.servicios.service.Impl;

import com.proyecto.servicios.entity.sf.Cliente;
import com.proyecto.servicios.entity.sf.Usuario;
import com.proyecto.servicios.exception.BusinessRuleException;
import com.proyecto.servicios.exception.DuplicateResourceException;
import com.proyecto.servicios.exception.ResourceNotFoundException;
import com.proyecto.servicios.model.UsuarioActualizaRequest;
import com.proyecto.servicios.model.UsuarioBusquedaRequest;
import com.proyecto.servicios.model.UsuarioEliminaRequest;
import com.proyecto.servicios.model.UsuarioRegistroRequest;
import com.proyecto.servicios.model.UsuarioResponse;
import com.proyecto.servicios.repositorys.sf.ClienteRepository;
import com.proyecto.servicios.repositorys.sf.UsuarioRepository;
import com.proyecto.servicios.repositorys.sf.UsuarioSpecification;
import com.proyecto.servicios.service.UsuarioService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class UsuarioServiceImpl implements UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final ClienteRepository clienteRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioServiceImpl(
            UsuarioRepository usuarioRepository,
            ClienteRepository clienteRepository,
            PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.clienteRepository = clienteRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public UsuarioResponse registrarUsuario(UsuarioRegistroRequest request) {
        String correoNormalizado = request.getCorreo().trim().toLowerCase();
        log.info("Iniciando registro de usuario para correo: {}", correoNormalizado);

        if (usuarioRepository.existsByCorreo(correoNormalizado)) {
            throw new DuplicateResourceException("El correo '" + correoNormalizado + "' ya se encuentra registrado en el sistema");
        }

        Cliente cliente = clienteRepository.findById(request.getClienteId())
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado con ID: " + request.getClienteId()));

        if (usuarioRepository.existsByClienteId(cliente.getId())) {
            throw new DuplicateResourceException("El cliente con ID " + cliente.getId() + " ya cuenta con un usuario registrado");
        }

        String hash = passwordEncoder.encode(request.getPassword());

        Usuario nuevoUsuario = Usuario.builder()
                .correo(correoNormalizado)
                .passwordHash(hash)
                .rol(request.getRol().trim().toUpperCase())
                .activo(true)
                .cliente(cliente)
                .build();

        Usuario guardado = usuarioRepository.save(nuevoUsuario);
        log.info("Usuario registrado exitosamente con ID: {} para cliente ID: {}", guardado.getId(), cliente.getId());

        return mapearResponse(guardado);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UsuarioResponse> consultarUsuarios(UsuarioBusquedaRequest filtro) {
        if (filtro == null || ((filtro.getRfc() == null || filtro.getRfc().isBlank())
                && (filtro.getCurp() == null || filtro.getCurp().isBlank()))) {
            throw new BusinessRuleException("Debe proporcionar al menos un criterio de búsqueda válido (RFC o CURP). No se permiten consultas sin filtros.");
        }

        log.debug("Ejecutando consulta de usuarios filtrada exclusivamente por RFC y CURP");

        Specification<Usuario> spec = Specification.where(null);
        spec = spec
                .and(UsuarioSpecification.conRfc(filtro.getRfc()))
                .and(UsuarioSpecification.conCurp(filtro.getCurp()));

        List<Usuario> resultados = usuarioRepository.findAll(spec);
        log.debug("Se encontraron {} usuarios que coinciden con los criterios de búsqueda (RFC/CURP)", resultados.size());

        return resultados.stream()
                .map(this::mapearResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public UsuarioResponse actualizarUsuario(UsuarioActualizaRequest request) {
        log.info("Iniciando actualización para usuario ID: {}", request.getId());

        Usuario usuario = usuarioRepository.findById(request.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con ID: " + request.getId()));

        if (request.getCorreo() != null && !request.getCorreo().isBlank()) {
            String nuevoCorreo = request.getCorreo().trim().toLowerCase();
            if (!nuevoCorreo.equalsIgnoreCase(usuario.getCorreo())) {
                if (usuarioRepository.existsByCorreo(nuevoCorreo)) {
                    throw new DuplicateResourceException("El correo '" + nuevoCorreo + "' ya está registrado por otro usuario");
                }
                usuario.setCorreo(nuevoCorreo);
            }
        }

        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            usuario.setPasswordHash(passwordEncoder.encode(request.getPassword()));
            log.debug("Contraseña actualizada y rehasheada para usuario ID: {}", usuario.getId());
        }

        if (request.getRol() != null && !request.getRol().isBlank()) {
            usuario.setRol(request.getRol().trim().toUpperCase());
        }

        if (request.getActivo() != null) {
            usuario.setActivo(request.getActivo());
        }

        Usuario actualizado = usuarioRepository.save(usuario);
        log.info("Usuario ID: {} actualizado exitosamente", actualizado.getId());

        return mapearResponse(actualizado);
    }

    @Override
    @Transactional
    public void eliminarUsuario(UsuarioEliminaRequest request) {
        log.info("Procesando baja lógica para usuario ID: {}. Motivo: {}", request.getId(), request.getMotivo());

        Usuario usuario = usuarioRepository.findById(request.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con ID: " + request.getId()));

        if (Boolean.FALSE.equals(usuario.getActivo())) {
            throw new BusinessRuleException("El usuario con ID " + request.getId() + " ya se encuentra inhabilitado");
        }

        usuario.setActivo(false);
        usuarioRepository.save(usuario);
        log.info("Baja lógica aplicada satisfactoriamente al usuario ID: {}. Motivo registrado: {}", request.getId(), request.getMotivo());
    }

    private UsuarioResponse mapearResponse(Usuario usuario) {
        return UsuarioResponse.builder()
                .id(usuario.getId())
                .correo(usuario.getCorreo())
                .rol(usuario.getRol())
                .activo(usuario.getActivo())
                .clienteId(usuario.getCliente() != null ? usuario.getCliente().getId() : null)
                .fechaCreacion(usuario.getFechaCreacion())
                .fechaActualizacion(usuario.getFechaActualizacion())
                .build();
    }
}
