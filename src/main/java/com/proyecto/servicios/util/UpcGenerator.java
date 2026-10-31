package com.proyecto.servicios.util;

import com.proyecto.servicios.repositorys.sf.TransaccionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@Component
@RequiredArgsConstructor
public class UpcGenerator {

    private final TransaccionRepository transaccionRepository;
    private final AtomicLong fallbackCounter = new AtomicLong(System.currentTimeMillis() % 10000000000L);

    public String generarUpc() {
        try {
            Long nextSeq = transaccionRepository.obtenerSiguienteValorSecuenciaUpc();
            if (nextSeq != null) {
                // Formato U + 12 dígitos = 13 caracteres (requisito oficial: 3 a 15)
                return "U" + String.format("%012d", nextSeq);
            }
        } catch (Exception e) {
            log.warn("No fue posible obtener secuencia de BD seq_upc, utilizando generador atómico alterno: {}", e.getMessage());
        }

        long fallback = fallbackCounter.incrementAndGet();
        return "U" + String.format("%012d", fallback);
    }
}

