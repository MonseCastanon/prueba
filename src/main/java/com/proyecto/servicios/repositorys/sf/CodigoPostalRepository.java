package com.proyecto.servicios.repositorys.sf;

import com.proyecto.servicios.entity.sf.CodigoPostalCatalogo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CodigoPostalRepository extends JpaRepository<CodigoPostalCatalogo, Long> {
    List<CodigoPostalCatalogo> findByCodigoPostalAndActivoTrue(String codigoPostal);
}
