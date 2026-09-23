package com.proyecto.servicios.cron;

import com.proyecto.servicios.service.CatProductoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Tarea programada (Cron Task) encargada de actualizar el catálogo
 * de productos consumiendo la API de GP todos los días a las 6:00 AM.
 */
@Component
public class CatProductoCronTask {

    private static final Logger log = LoggerFactory.getLogger(CatProductoCronTask.class);

    private final CatProductoService catProductoService;

    public CatProductoCronTask(CatProductoService catProductoService) {
        this.catProductoService = catProductoService;
    }

    /**
     * Cron expression: "0 0 6 * * *" -> Segundo 0, Minuto 0, Hora 6 (6:00 AM), todos los días.
     */
    @Scheduled(
            cron = "${gestopago.cron.get-product-list:${gestopago.cron.cat-product:0 0 6 * * *}}",
            zone = "${gestopago.cron.timezone:America/Mexico_City}"
    )
    public void ejecutarSincronizacionCatalogo() {
        log.info("[CRON TASK 6:00 AM] Iniciando tarea programada de actualización de catProduct");
        try {
            catProductoService.sincronizarCatalogoDesdeGp();
            log.info("[CRON TASK 6:00 AM] Tarea programada completada con éxito");
        } catch (Exception e) {
            log.error("[CRON TASK ERROR] Fallo durante la ejecución del cron de catProduct: {}", e.getMessage(), e);
        }
    }
}
