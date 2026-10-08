package com.proyecto.servicios.service.Impl;

import com.proyecto.servicios.entity.sf.Cliente;
import com.proyecto.servicios.entity.sf.Cuenta;
import com.proyecto.servicios.enums.EstatusCuenta;
import com.proyecto.servicios.exception.BusinessRuleException;
import com.proyecto.servicios.exception.ResourceNotFoundException;
import com.proyecto.servicios.model.*;
import com.proyecto.servicios.repositorys.sf.ClienteRepository;
import com.proyecto.servicios.repositorys.sf.CuentaRepository;
import com.proyecto.servicios.service.CuentaService;
import com.proyecto.servicios.util.GeneradorCuentaUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class CuentaServiceImpl implements CuentaService {

    private final CuentaRepository cuentaRepository;
    private final ClienteRepository clienteRepository;
    private final GeneradorCuentaUtil generadorCuentaUtil;

    @Value("${banco.onboarding.prefijo-cuenta:9080}")
    private String prefijoCuenta;

    public CuentaServiceImpl(CuentaRepository cuentaRepository,
            ClienteRepository clienteRepository,
            GeneradorCuentaUtil generadorCuentaUtil) {
        this.cuentaRepository = cuentaRepository;
        this.clienteRepository = clienteRepository;
        this.generadorCuentaUtil = generadorCuentaUtil;
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED, rollbackFor = Exception.class)
    public CuentaResponse crearCuentaParaCliente(Long clienteId, BigDecimal saldoInicial) {
        Cliente cliente = clienteRepository.findById(clienteId)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado con ID: " + clienteId));

        if (!cliente.getActivo()) {
            throw new BusinessRuleException("No se pueden aperturar cuentas para clientes en estatus inactivo");
        }

        if (saldoInicial != null && saldoInicial.compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessRuleException("El saldo inicial no puede ser negativo");
        }

        String numero = generarNumeroCuentaUnico();
        Cuenta cuenta = Cuenta.builder()
                .cliente(cliente)
                .numeroCuenta(numero)
                .saldo(saldoInicial != null ? saldoInicial : BigDecimal.ZERO)
                .estatus(EstatusCuenta.ACTIVA)
                .activa(true)
                .build();
        cuenta.agregarEventoHistorial("APERTURA", "Apertura de cuenta adicional", "OPERADOR");

        Cuenta guardada = cuentaRepository.save(cuenta);
        return mapearCuentaResponse(guardada);
    }

    @Override
    @Transactional(readOnly = true)
    public CuentaResponse consultarPorNumero(String numeroCuenta) {
        return mapearCuentaResponse(buscarPorNumero(numeroCuenta));
    }

    @Override
    @Transactional(readOnly = true)
    public List<CuentaResponse> consultarPorClienteId(Long clienteId) {
        return cuentaRepository.findByClienteId(clienteId).stream()
                .map(this::mapearCuentaResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CuentaResponse> consultarPorEstatus(EstatusCuenta estatus) {
        return cuentaRepository.findByEstatus(estatus).stream()
                .map(this::mapearCuentaResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CuentaResponse> consultarCuentasActivas() {
        return cuentaRepository.findByActivaTrue().stream()
                .map(this::mapearCuentaResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal consultarSaldo(String numeroCuenta) {
        Cuenta cuenta = buscarPorNumero(numeroCuenta);
        return cuenta.getSaldo();
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED, rollbackFor = Exception.class)
    public CuentaResponse bloquearCuenta(String numeroCuenta, BloqueoDesbloqueoCuentaRequest request) {
        Cuenta cuenta = buscarPorNumero(numeroCuenta);

        if (cuenta.getEstatus() == EstatusCuenta.BLOQUEADA) {
            throw new BusinessRuleException("La cuenta " + numeroCuenta + " ya se encuentra bloqueada");
        }
        if (cuenta.getEstatus() == EstatusCuenta.CANCELADA) {
            throw new BusinessRuleException("No se puede bloquear una cuenta cancelada");
        }

        cuenta.setEstatus(EstatusCuenta.BLOQUEADA);
        cuenta.setActiva(false);
        cuenta.setFechaBloqueo(OffsetDateTime.now());
        cuenta.setMotivoBloqueo(request.getMotivo());
        cuenta.agregarEventoHistorial("BLOQUEO", request.getMotivo(), request.getUsuarioOperador());

        return mapearCuentaResponse(cuentaRepository.save(cuenta));
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED, rollbackFor = Exception.class)
    public CuentaResponse desbloquearCuenta(String numeroCuenta, BloqueoDesbloqueoCuentaRequest request) {
        Cuenta cuenta = buscarPorNumero(numeroCuenta);

        if (cuenta.getEstatus() != EstatusCuenta.BLOQUEADA) {
            throw new BusinessRuleException("La cuenta " + numeroCuenta + " no está bloqueada (Estatus: " + cuenta.getEstatus() + ")");
        }
        if (!cuenta.getCliente().getActivo()) {
            throw new BusinessRuleException("No se puede desbloquear una cuenta si el cliente titular se encuentra inactivo");
        }

        cuenta.setEstatus(EstatusCuenta.ACTIVA);
        cuenta.setActiva(true);
        cuenta.setFechaBloqueo(null);
        cuenta.setMotivoBloqueo(null);
        cuenta.agregarEventoHistorial("DESBLOQUEO", request.getMotivo(), request.getUsuarioOperador());

        return mapearCuentaResponse(cuentaRepository.save(cuenta));
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED, rollbackFor = Exception.class)
    public CuentaResponse patchCuenta(String numeroCuenta, CuentaPatchRequest request) {
        Cuenta cuenta = buscarPorNumero(numeroCuenta);

        if (request.getSaldo() != null) {
            cuenta.setSaldo(request.getSaldo());
        }

        if (request.getEstatus() != null && request.getEstatus() != cuenta.getEstatus()) {
            if (request.getEstatus() == EstatusCuenta.BLOQUEADA) {
                bloquearCuenta(numeroCuenta, new BloqueoDesbloqueoCuentaRequest(request.getMotivo(), "ADMIN_PATCH"));
            } else if (request.getEstatus() == EstatusCuenta.ACTIVA) {
                desbloquearCuenta(numeroCuenta, new BloqueoDesbloqueoCuentaRequest(request.getMotivo(), "ADMIN_PATCH"));
            }
        }

        return mapearCuentaResponse(cuentaRepository.save(cuenta));
    }

    private Cuenta buscarPorNumero(String numeroCuenta) {
        return cuentaRepository.findByNumeroCuenta(numeroCuenta.trim())
                .orElseThrow(() -> new ResourceNotFoundException("Cuenta bancaria no encontrada: " + numeroCuenta));
    }

    private String generarNumeroCuentaUnico() {
        String num;
        do {
            num = generadorCuentaUtil.generarNumeroCuenta(prefijoCuenta);
        } while (cuentaRepository.existsByNumeroCuenta(num));
        return num;
    }

    private CuentaResponse mapearCuentaResponse(Cuenta c) {
        return CuentaResponse.builder()
                .id(c.getId())
                .clienteId(c.getCliente().getId())
                .nombreTitular(c.getCliente().getNombre() + " " + c.getCliente().getApellidoPaterno())
                .numeroCuenta(c.getNumeroCuenta())
                .saldo(c.getSaldo())
                .estatus(c.getEstatus().name())
                .activa(c.getActiva())
                .fechaCreacion(c.getFechaCreacion())
                .fechaBloqueo(c.getFechaBloqueo())
                .motivoBloqueo(c.getMotivoBloqueo())
                .build();
    }
}
