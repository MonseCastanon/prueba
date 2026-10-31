package com.proyecto.servicios.service;

import com.proyecto.servicios.config.security.AuthenticatedUser;
import com.proyecto.servicios.entity.sf.Cuenta;
import com.proyecto.servicios.entity.sf.Movimiento;
import com.proyecto.servicios.entity.sf.Transaccion;
import com.proyecto.servicios.enums.EstadoTransaccion;
import com.proyecto.servicios.enums.EstatusCuenta;
import com.proyecto.servicios.enums.TipoMovimiento;
import com.proyecto.servicios.exception.AccountBlockedException;
import com.proyecto.servicios.exception.DuplicateRequestException;
import com.proyecto.servicios.exception.InsufficientFundsException;
import com.proyecto.servicios.exception.ResourceNotFoundException;
import com.proyecto.servicios.model.PagoServicioRequest;
import com.proyecto.servicios.repositorys.sf.CuentaRepository;
import com.proyecto.servicios.repositorys.sf.MovimientoRepository;
import com.proyecto.servicios.repositorys.sf.TransaccionRepository;
import com.proyecto.servicios.util.Masker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PagoTxService {

    private final CuentaRepository cuentaRepository;
    private final TransaccionRepository transaccionRepository;
    private final MovimientoRepository movimientoRepository;

    @Transactional(isolation = Isolation.READ_COMMITTED, rollbackFor = Exception.class)
    public Transaccion reservarFondos(PagoServicioRequest req,
                                      BigDecimal montoFinal,
                                      AuthenticatedUser user,
                                      String requestHash,
                                      String upc,
                                      String horaLocal) {

        Cuenta cuenta = cuentaRepository.findByNumeroCuentaForUpdate(req.numeroCuenta().trim())
                .orElseThrow(() -> new ResourceNotFoundException("Cuenta bancaria no encontrada: " + req.numeroCuenta()));

        // Control Anti-IDOR: validar que la cuenta pertenezca al usuario autenticado (salvo ROLE_ADMIN)
        if (user.clienteId() != null && !user.clienteId().equals(cuenta.getCliente().getId())) {
            log.warn("Intento de acceso a cuenta no autorizada. Cliente autenticado: {}, Cuenta pertenece a: {}",
                    user.clienteId(), cuenta.getCliente().getId());
            throw new ResourceNotFoundException("Cuenta bancaria no encontrada: " + req.numeroCuenta());
        }

        // Validar estatus de la cuenta y titular
        if (cuenta.getEstatus() != EstatusCuenta.ACTIVA || !Boolean.TRUE.equals(cuenta.getActiva())
                || (cuenta.getCliente() != null && !Boolean.TRUE.equals(cuenta.getCliente().getActivo()))) {
            throw new AccountBlockedException("La cuenta bancaria o su titular no se encuentran activos para operar.");
        }

        // Control de idempotencia serializado bajo el bloqueo pesimista de la cuenta
        java.util.Optional<Transaccion> existenteBajoLock = transaccionRepository.findByIdDocumento(req.idDocumento().trim());
        if (existenteBajoLock.isPresent()) {
            Transaccion previa = existenteBajoLock.get();
            if (!previa.getRequestHash().equals(requestHash)) {
                throw new DuplicateRequestException("El IdDocumento especificado ya fue utilizado para otra transacción con parámetros distintos.");
            }
            return previa;
        }

        // Validar saldo suficiente
        if (cuenta.getSaldo().compareTo(montoFinal) < 0) {
            log.warn("Saldo insuficiente para cuenta {}: saldo actual={}, monto={}",
                    Masker.enmascararCuenta(cuenta.getNumeroCuenta()), cuenta.getSaldo(), montoFinal);
            throw new InsufficientFundsException("Saldo insuficiente en la cuenta para realizar el pago de $" + montoFinal);
        }

        UUID txId = UUID.randomUUID();

        // Crear la transacción en estado PENDIENTE (retención de fondos previa al envío)
        Transaccion transaccion = Transaccion.builder()
                .id(txId)
                .cuenta(cuenta)
                .idDocumento(req.idDocumento().trim())
                .upc(upc)
                .horaLocal(horaLocal)
                .requestHash(requestHash)
                .idProducto(req.idProducto())
                .idServicio(req.idServicio())
                .referenciaEnmascarada(Masker.enmascararReferencia(req.referencia()))
                .monto(montoFinal)
                .estado(EstadoTransaccion.PENDIENTE)
                .intentosConciliacion((short) 0)
                .fechaCreacion(OffsetDateTime.now())
                .build();

        Transaccion guardada = transaccionRepository.saveAndFlush(transaccion);

        // Descontar saldo de la cuenta bajo bloqueo pesimista
        BigDecimal saldoAnterior = cuenta.getSaldo();
        BigDecimal saldoPosterior = saldoAnterior.subtract(montoFinal);
        cuenta.setSaldo(saldoPosterior);
        cuentaRepository.save(cuenta);

        // Registrar en el ledger inmutable (movimientos_cuenta)
        Movimiento movDebito = Movimiento.builder()
                .cuenta(cuenta)
                .transaccion(guardada)
                .tipo(TipoMovimiento.DEBITO)
                .monto(montoFinal)
                .saldoAnterior(saldoAnterior)
                .saldoPosterior(saldoPosterior)
                .fecha(OffsetDateTime.now())
                .build();
        movimientoRepository.save(movDebito);

        log.info("Fondos retenidos con éxito: txId={}, upc={}, monto={}, nuevoSaldo={}",
                txId, upc, montoFinal, saldoPosterior);

        return guardada;
    }

    @Transactional(isolation = Isolation.READ_COMMITTED, rollbackFor = Exception.class)
    public void marcarEnviada(UUID txId, String payloadCifrado) {
        transaccionRepository.findById(txId).ifPresent(tx -> {
            if (tx.getEstado().puedePasarA(EstadoTransaccion.ENVIADA)) {
                tx.setEstado(EstadoTransaccion.ENVIADA);
                tx.setFechaEnvio(OffsetDateTime.now());
                tx.setPayloadCifrado(payloadCifrado);
                transaccionRepository.save(tx);
                log.info("Transacción {} marcada como ENVIADA", txId);
            }
        });
    }

    @Transactional(isolation = Isolation.READ_COMMITTED, rollbackFor = Exception.class)
    public void confirmarExitosa(UUID txId, String numAutorizacion, String idTx, String codigo) {
        transaccionRepository.findById(txId).ifPresent(tx -> {
            if (tx.getEstado() == EstadoTransaccion.EXITOSA) {
                return; // Idempotente
            }
            if (tx.getEstado().puedePasarA(EstadoTransaccion.EXITOSA)) {
                tx.setEstado(EstadoTransaccion.EXITOSA);
                tx.setAutorizacionProveedor(numAutorizacion);
                tx.setIdTicketProveedor(idTx);
                tx.setCodigoProveedor(codigo);
                tx.setFechaResolucion(OffsetDateTime.now());
                transaccionRepository.save(tx);
                log.info("Transacción {} confirmada como EXITOSA. Autorización: {}", txId, numAutorizacion);
            } else {
                log.warn("Transición no válida a EXITOSA para tx {} desde estado {}", txId, tx.getEstado());
            }
        });
    }

    @Transactional(isolation = Isolation.READ_COMMITTED, rollbackFor = Exception.class)
    public void reversarTransaccion(UUID txId, String motivo) {
        transaccionRepository.findById(txId).ifPresent(tx -> {
            if (tx.getEstado() == EstadoTransaccion.REVERSADA) {
                return; // Idempotente
            }
            if (!tx.getEstado().puedePasarA(EstadoTransaccion.REVERSADA)) {
                log.warn("Transición no válida a REVERSADA para tx {} desde estado {}", txId, tx.getEstado());
                return;
            }

            // Evitar doble reverso a nivel ledger
            if (movimientoRepository.existsByTransaccionIdAndTipo(txId, TipoMovimiento.REVERSO)) {
                log.warn("Ya existe un movimiento de reverso para la transacción {}", txId);
                tx.setEstado(EstadoTransaccion.REVERSADA);
                transaccionRepository.save(tx);
                return;
            }

            Cuenta cuenta = cuentaRepository.findByNumeroCuentaForUpdate(tx.getCuenta().getNumeroCuenta())
                    .orElseThrow(() -> new ResourceNotFoundException("Cuenta bancaria no encontrada: " + tx.getCuenta().getNumeroCuenta()));

            BigDecimal saldoAnterior = cuenta.getSaldo();
            BigDecimal saldoPosterior = saldoAnterior.add(tx.getMonto());
            cuenta.setSaldo(saldoPosterior);
            cuentaRepository.save(cuenta);

            Movimiento movReverso = Movimiento.builder()
                    .cuenta(cuenta)
                    .transaccion(tx)
                    .tipo(TipoMovimiento.REVERSO)
                    .monto(tx.getMonto())
                    .saldoAnterior(saldoAnterior)
                    .saldoPosterior(saldoPosterior)
                    .fecha(OffsetDateTime.now())
                    .build();
            movimientoRepository.save(movReverso);

            tx.setEstado(EstadoTransaccion.REVERSADA);
            tx.setCodigoProveedor(motivo);
            tx.setFechaResolucion(OffsetDateTime.now());
            transaccionRepository.save(tx);

            log.info("Transacción {} REVERSADA exitosamente. Saldo reincorporado: {}, nuevoSaldo: {}",
                    txId, tx.getMonto(), saldoPosterior);
        });
    }

    @Transactional(isolation = Isolation.READ_COMMITTED, rollbackFor = Exception.class)
    public void marcarIncierta(UUID txId, String codigo, String motivo) {
        transaccionRepository.findById(txId).ifPresent(tx -> {
            if (tx.getEstado() == EstadoTransaccion.INCIERTA) {
                return;
            }
            if (tx.getEstado().puedePasarA(EstadoTransaccion.INCIERTA)) {
                tx.setEstado(EstadoTransaccion.INCIERTA);
                tx.setCodigoProveedor(codigo);
                tx.setProximoIntentoEn(OffsetDateTime.now().plusSeconds(65));
                transaccionRepository.save(tx);
                log.info("Transacción {} marcada como INCIERTA (los fondos continúan retenidos para conciliación)", txId);
            }
        });
    }
}
