package com.proyecto.servicios.repositorys.sf;

import com.proyecto.servicios.entity.sf.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long>, JpaSpecificationExecutor<Usuario> {

    Optional<Usuario> findByCorreo(String correo);

    boolean existsByCorreo(String correo);

    Optional<Usuario> findByClienteId(Long clienteId);

    boolean existsByClienteId(Long clienteId);

    List<Usuario> findByActivo(Boolean activo);
}
