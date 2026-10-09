package com.proyecto.servicios.repositorys.sf;

import com.proyecto.servicios.entity.sf.Pais;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaisRepository extends JpaRepository<Pais, Long> {
    Optional<Pais> findByCodigoIsoAlfa3(String codigoIsoAlfa3);
    List<Pais> findByActivoTrue();
}
