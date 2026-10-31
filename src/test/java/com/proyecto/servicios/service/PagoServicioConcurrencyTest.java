package com.proyecto.servicios.service;

import com.proyecto.servicios.config.security.AuthenticatedUser;
import com.proyecto.servicios.entity.sf.Cliente;
import com.proyecto.servicios.entity.sf.Cuenta;
import com.proyecto.servicios.entity.sf.Movimiento;
import com.proyecto.servicios.entity.sf.Transaccion;
import com.proyecto.servicios.enums.EstadoTransaccion;
import com.proyecto.servicios.enums.EstatusCuenta;
import com.proyecto.servicios.enums.TipoMovimiento;
import com.proyecto.servicios.exception.InsufficientFundsException;
import com.proyecto.servicios.model.PagoServicioRequest;
import com.proyecto.servicios.repositorys.sf.CuentaRepository;
import com.proyecto.servicios.repositorys.sf.MovimientoRepository;
import com.proyecto.servicios.repositorys.sf.TransaccionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PagoServicioConcurrencyTest {

    @Mock
    private CuentaRepository cuentaRepository;
    @Mock
    private TransaccionRepository transaccionRepository;
    @Mock
    private MovimientoRepository movimientoRepository;

    @InjectMocks
    private PagoTxService pagoTxService;

    private AuthenticatedUser authUser;
    private Cliente cliente;
    private Cuenta cuentaReal;

    @BeforeEach
    void setUp() {
        cliente = Cliente.builder().id(1001L).activo(true).build();
        authUser = new AuthenticatedUser(
                1L, 1001L, "cliente@banco.com", "passHash", "ROLE_CLIENTE", Collections.emptyList(), true);

        // Cuenta con saldo únicamente para UNA transacción de $100
        cuentaReal = Cuenta.builder()
                .id(10L)
                .numeroCuenta("9080512384")
                .cliente(cliente)
                .saldo(new BigDecimal("100.00"))
                .estatus(EstatusCuenta.ACTIVA)
                .activa(true)
                .build();
    }

    @Test
    @DisplayName("Concurrencia de saldo: 10 hilos intentan pagar $100 con saldo para 1 solo pago -> Exactamente 1 éxito y saldo final no negativo")
    void testConcurrenciaReservaSaldo() throws Exception {
        int totalHilos = 10;
        ExecutorService executor = Executors.newFixedThreadPool(totalHilos);
        CountDownLatch latch = new CountDownLatch(1);
        AtomicInteger exitos = new AtomicInteger(0);
        AtomicInteger fallosSaldo = new AtomicInteger(0);

        // Simulamos el bloqueo pesimista sincronizando el acceso a cuentaReal
        when(cuentaRepository.findByNumeroCuentaForUpdate("9080512384")).thenAnswer(invocation -> {
            // Devuelve la misma cuenta sincronizada
            return Optional.of(cuentaReal);
        });

        when(transaccionRepository.saveAndFlush(any(Transaccion.class))).thenAnswer(invocation -> {
            Transaccion t = invocation.getArgument(0);
            return t;
        });

        // Envoltorio sincronizado que simula el SELECT FOR UPDATE de PostgreSQL
        synchronized (cuentaReal) {
            // Bloque sincronizado simula serialización por FOR UPDATE
        }

        // Servicio con cerrojo de serialización para simular el bloqueo a nivel base de datos
        PagoTxService serviceSincronizado = new PagoTxService(cuentaRepository, transaccionRepository, movimientoRepository) {
            @Override
            public synchronized Transaccion reservarFondos(PagoServicioRequest req, BigDecimal montoFinal, AuthenticatedUser user, String requestHash, String upc, String horaLocal) {
                return super.reservarFondos(req, montoFinal, user, requestHash, upc, horaLocal);
            }
        };

        List<Future<?>> futures = new ArrayList<>();

        for (int i = 0; i < totalHilos; i++) {
            final int index = i;
            futures.add(executor.submit(() -> {
                try {
                    latch.await();
                    PagoServicioRequest req = new PagoServicioRequest(
                            "9080512384", 5, 101, "DOC-CONC-" + index, "REF-" + index, new BigDecimal("100.00"), "1234");

                    serviceSincronizado.reservarFondos(
                            req, new BigDecimal("100.00"), authUser, "hash-" + index, "U00000000" + index, "20261028143000");

                    exitos.incrementAndGet();
                } catch (InsufficientFundsException e) {
                    fallosSaldo.incrementAndGet();
                } catch (Exception e) {
                    fail("Error inesperado en concurrencia: " + e.getMessage());
                }
            }));
        }

        latch.countDown(); // Disparar todos los hilos simultáneamente

        for (Future<?> f : futures) {
            f.get(5, TimeUnit.SECONDS);
        }

        executor.shutdown();

        // Exactamente 1 transacción debe tener éxito
        assertEquals(1, exitos.get(), "Solo 1 transacción debió ser autorizada");
        // Los otros 9 hilos deben fallar con saldo insuficiente
        assertEquals(9, fallosSaldo.get(), "9 transacciones debieron fallar por saldo insuficiente");
        // El saldo final debe ser exactamente 0.00, nunca negativo
        assertEquals(new BigDecimal("0.00"), cuentaReal.getSaldo());
        assertTrue(cuentaReal.getSaldo().compareTo(BigDecimal.ZERO) >= 0);

        verify(movimientoRepository, times(1)).save(any(Movimiento.class));
    }

    @Test
    @DisplayName("Prevención de doble reverso: llamar dos veces a reversarTransaccion tiene efecto una sola vez")
    void testPrevencionDobleReverso() {
        UUID txId = UUID.randomUUID();
        Transaccion tx = Transaccion.builder()
                .id(txId)
                .cuenta(cuentaReal)
                .monto(new BigDecimal("50.00"))
                .estado(EstadoTransaccion.ENVIADA)
                .build();

        when(transaccionRepository.findById(txId)).thenReturn(Optional.of(tx));
        when(cuentaRepository.findByNumeroCuentaForUpdate("9080512384")).thenReturn(Optional.of(cuentaReal));
        when(movimientoRepository.existsByTransaccionIdAndTipo(txId, TipoMovimiento.REVERSO))
                .thenReturn(false) // Primera llamada: no existe
                .thenReturn(true);  // Segunda llamada: ya existe

        // Primer reverso
        pagoTxService.reversarTransaccion(txId, "Rechazado");
        assertEquals(new BigDecimal("150.00"), cuentaReal.getSaldo());
        assertEquals(EstadoTransaccion.REVERSADA, tx.getEstado());

        // Segundo reverso (idempotente)
        pagoTxService.reversarTransaccion(txId, "Rechazado");
        // Saldo no se duplica
        assertEquals(new BigDecimal("150.00"), cuentaReal.getSaldo());
        verify(movimientoRepository, times(1)).save(any(Movimiento.class));
    }

    @Test
    @DisplayName("Concurrencia idempotente: 10 hilos con el mismo idDocumento y saldo de $1000 -> Exactamente 1 débito de $100 (saldo final $900)")
    void testConcurrenciaIdempotenteMismoIdDocumento() throws Exception {
        cuentaReal.setSaldo(new BigDecimal("1000.00"));
        int totalHilos = 10;
        ExecutorService executor = Executors.newFixedThreadPool(totalHilos);
        CountDownLatch latch = new CountDownLatch(1);

        final java.util.concurrent.atomic.AtomicReference<Transaccion> guardadaRef = new java.util.concurrent.atomic.AtomicReference<>();

        when(cuentaRepository.findByNumeroCuentaForUpdate("9080512384")).thenReturn(Optional.of(cuentaReal));
        when(transaccionRepository.findByIdDocumento("DOC-IDEMP-UNICO")).thenAnswer(inv -> Optional.ofNullable(guardadaRef.get()));
        when(transaccionRepository.saveAndFlush(any(Transaccion.class))).thenAnswer(inv -> {
            Transaccion t = inv.getArgument(0);
            guardadaRef.set(t);
            return t;
        });

        PagoTxService serviceSincronizado = new PagoTxService(cuentaRepository, transaccionRepository, movimientoRepository) {
            @Override
            public synchronized Transaccion reservarFondos(PagoServicioRequest req, BigDecimal montoFinal, AuthenticatedUser user, String requestHash, String upc, String horaLocal) {
                return super.reservarFondos(req, montoFinal, user, requestHash, upc, horaLocal);
            }
        };

        List<Future<Transaccion>> futures = new ArrayList<>();
        for (int i = 0; i < totalHilos; i++) {
            final int index = i;
            futures.add(executor.submit(() -> {
                latch.await();
                PagoServicioRequest req = new PagoServicioRequest(
                        "9080512384", 5, 101, "DOC-IDEMP-UNICO", "REF-MISMA", new BigDecimal("100.00"), "1234");
                return serviceSincronizado.reservarFondos(
                        req, new BigDecimal("100.00"), authUser, "hash-mismo", "U00000000" + index, "20261028143000");
            }));
        }

        latch.countDown();

        Set<UUID> txIds = new HashSet<>();
        for (Future<Transaccion> f : futures) {
            Transaccion t = f.get(5, TimeUnit.SECONDS);
            assertNotNull(t);
            txIds.add(t.getId());
        }

        executor.shutdown();

        // Todos los 10 hilos deben recibir exactamente la misma transacción
        assertEquals(1, txIds.size(), "Los 10 hilos deben recibir el mismo ID de transacción");
        // Solo se debió descontar $100 una única vez (saldo final $900.00)
        assertEquals(new BigDecimal("900.00"), cuentaReal.getSaldo());
        verify(movimientoRepository, times(1)).save(any(Movimiento.class));
    }
}

