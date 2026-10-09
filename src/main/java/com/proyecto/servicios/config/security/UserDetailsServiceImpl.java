package com.proyecto.servicios.config.security;

import com.proyecto.servicios.entity.sf.Usuario;
import com.proyecto.servicios.repositorys.sf.UsuarioRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    public UserDetailsServiceImpl(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Usuario usuario = usuarioRepository.findByCorreo(username.toLowerCase().trim())
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado con correo: " + username));

        boolean habilitado = Boolean.TRUE.equals(usuario.getActivo()) &&
                (usuario.getCliente() == null || Boolean.TRUE.equals(usuario.getCliente().getActivo()));

        return new User(
                usuario.getCorreo(),
                usuario.getPasswordHash(),
                habilitado,
                true,
                true,
                true,
                Collections.singletonList(new SimpleGrantedAuthority(usuario.getRol()))
        );
    }
}
