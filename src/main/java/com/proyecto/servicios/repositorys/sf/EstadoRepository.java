package com.proyecto.servicios.repositorys.sf;

import com.proyecto.servicios.entity.sf.EstadoCatalogo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EstadoRepository extends JpaRepository<EstadoCatalogo, Long> {
    List<EstadoCatalogo> findByPaisCodigoIsoAlfa3AndActivoTrue(String paisIso);
    List<EstadoCatalogo> findByActivoTrue();
}
