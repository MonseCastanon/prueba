package com.proyecto.servicios.repositorys.sf;

import com.proyecto.servicios.entity.sf.Transaccion;
import com.proyecto.servicios.enums.EstadoTransaccion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TransaccionRepository extends JpaRepository<Transaccion, UUID> {

    Optional<Transaccion> findByCuentaIdAndIdDocumento(Long cuentaId, String idDocumento);

    Optional<Transaccion> findByIdDocumento(String idDocumento);

    Optional<Transaccion> findByUpc(String upc);

    List<Transaccion> findTop50ByEstadoInAndFechaEnvioBetweenOrderByFechaEnvioAsc(
            Collection<EstadoTransaccion> estados,
            OffsetDateTime desde,
            OffsetDateTime hasta
    );

    @Modifying
    @org.springframework.transaction.annotation.Transactional
    @Query("UPDATE Transaccion t SET t.estado = com.proyecto.servicios.enums.EstadoTransaccion.REVISION_MANUAL, t.fechaResolucion = CURRENT_TIMESTAMP WHERE t.estado IN ('ENVIADA', 'INCIERTA') AND t.fechaEnvio < :limite")
    int expirarTransaccionesMayoresA24Horas(@Param("limite") OffsetDateTime limite);

    @Query(value = "SELECT nextval('seq_upc')", nativeQuery = true)
    Long obtenerSiguienteValorSecuenciaUpc();
}

