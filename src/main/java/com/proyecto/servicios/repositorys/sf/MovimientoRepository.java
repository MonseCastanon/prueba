package com.proyecto.servicios.repositorys.sf;

import com.proyecto.servicios.entity.sf.Movimiento;
import com.proyecto.servicios.enums.TipoMovimiento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface MovimientoRepository extends JpaRepository<Movimiento, Long> {

    boolean existsByTransaccionIdAndTipo(UUID transaccionId, TipoMovimiento tipo);

    List<Movimiento> findByCuentaIdOrderByFechaDesc(Long cuentaId);
}

