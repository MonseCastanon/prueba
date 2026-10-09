package com.proyecto.servicios.repositorys.sf;

import com.proyecto.servicios.entity.sf.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ClienteRepository extends JpaRepository<Cliente, Long>, JpaSpecificationExecutor<Cliente> {

    Optional<Cliente> findByCurp(String curp);
    Optional<Cliente> findByRfc(String rfc);
    Optional<Cliente> findByCorreo(String correo);

    boolean existsByCurp(String curp);
    boolean existsByRfc(String rfc);
    boolean existsByCorreo(String correo);

    List<Cliente> findByNombreContainingIgnoreCase(String nombre);
    List<Cliente> findByApellidoPaternoContainingIgnoreCase(String apellidoPaterno);
    List<Cliente> findByApellidoMaternoContainingIgnoreCase(String apellidoMaterno);
    List<Cliente> findByActivoTrue();

    @Query("SELECT c FROM Cliente c WHERE c.fechaCreacion BETWEEN :fechaInicio AND :fechaFin")
    List<Cliente> findClientesByRangoFechas(
            @Param("fechaInicio") OffsetDateTime fechaInicio,
            @Param("fechaFin") OffsetDateTime fechaFin
    );

    @Query("SELECT c FROM Cliente c JOIN c.cuentas cta WHERE cta.numeroCuenta = :numeroCuenta")
    Optional<Cliente> findByNumeroCuenta(@Param("numeroCuenta") String numeroCuenta);
}
