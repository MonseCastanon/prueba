package com.proyecto.servicios.cron;

import com.proyecto.servicios.entity.sf.Transaccion;
import com.proyecto.servicios.enums.EstadoTransaccion;
import com.proyecto.servicios.repositorys.sf.TransaccionRepository;
import com.proyecto.servicios.service.ConciliacionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ConciliacionJob {

    private final TransaccionRepository transaccionRepository;
    private final ConciliacionService conciliacionService;

    @Scheduled(fixedDelayString = "${gestopago.conciliacion.cron-rate-ms:30000}")
    @SchedulerLock(name = "jobConciliacionGestopago", lockAtMostFor = "PT2M", lockAtLeastFor = "PT10S")
    public void procesarTransaccionesInciertas() {
        OffsetDateTime ahora = OffsetDateTime.now();
        OffsetDateTime hace62Segundos = ahora.minusSeconds(62);
        OffsetDateTime hace24Horas = ahora.minusHours(24);

        List<Transaccion> pendientes = transaccionRepository
                .findTop50ByEstadoInAndFechaEnvioBetweenOrderByFechaEnvioAsc(
                        List.of(EstadoTransaccion.INCIERTA, EstadoTransaccion.ENVIADA),
                        hace24Horas,
                        hace62Segundos
                );

        if (!pendientes.isEmpty()) {
            log.info("Job de conciliación encontró {} transacciones para conciliar con PuntoRed", pendientes.size());
            for (Transaccion tx : pendientes) {
                try {
                    conciliacionService.conciliarTransaccion(tx);
                } catch (Exception e) {
                    log.error("Error al conciliar tx {}: {}", tx.getId(), e.getMessage());
                }
            }
        }

        try {
            int expiradas = transaccionRepository.expirarTransaccionesMayoresA24Horas(hace24Horas);
            if (expiradas > 0) {
                log.warn("Se marcaron {} transacciones en REVISION_MANUAL por exceder 24 horas sin resolución", expiradas);
            }
        } catch (Exception e) {
            log.error("Error al expirar transacciones mayores a 24 horas: {}", e.getMessage());
        }
    }
}

