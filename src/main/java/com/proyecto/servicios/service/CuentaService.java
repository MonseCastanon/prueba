package com.proyecto.servicios.service;

import com.proyecto.servicios.enums.EstatusCuenta;
import com.proyecto.servicios.model.*;

import java.math.BigDecimal;
import java.util.List;

public interface CuentaService {
    CuentaResponse crearCuentaParaCliente(Long clienteId, BigDecimal saldoInicial);
    CuentaResponse consultarPorNumero(String numeroCuenta);
    List<CuentaResponse> consultarPorClienteId(Long clienteId);
    List<CuentaResponse> consultarPorEstatus(EstatusCuenta estatus);
    List<CuentaResponse> consultarCuentasActivas();
    BigDecimal consultarSaldo(String numeroCuenta);
    CuentaResponse bloquearCuenta(String numeroCuenta, BloqueoDesbloqueoCuentaRequest request);
    CuentaResponse desbloquearCuenta(String numeroCuenta, BloqueoDesbloqueoCuentaRequest request);
    CuentaResponse patchCuenta(String numeroCuenta, CuentaPatchRequest request);
}
