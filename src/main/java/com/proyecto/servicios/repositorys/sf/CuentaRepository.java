package com.proyecto.servicios.repositorys.sf;

import com.proyecto.servicios.entity.sf.Cuenta;
import com.proyecto.servicios.enums.EstatusCuenta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface CuentaRepository extends JpaRepository<Cuenta, Long> {

    Optional<Cuenta> findByNumeroCuenta(String numeroCuenta);
    boolean existsByNumeroCuenta(String numeroCuenta);

    List<Cuenta> findByClienteId(Long clienteId);
    List<Cuenta> findByEstatus(EstatusCuenta estatus);
    List<Cuenta> findByActivaTrue();

    @Query("SELECT c.saldo FROM Cuenta c WHERE c.numeroCuenta = :numeroCuenta")
    Optional<BigDecimal> findSaldoByNumeroCuenta(@Param("numeroCuenta") String numeroCuenta);
}
