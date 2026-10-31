package com.proyecto.servicios.service;

import com.proyecto.servicios.entity.sf.Transaccion;
import com.proyecto.servicios.exception.DuplicateRequestException;
import com.proyecto.servicios.repositorys.sf.TransaccionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class IdempotenciaService {

    private final TransaccionRepository transaccionRepository;

    public String calcularRequestHash(String numeroCuenta, Integer idProducto, Integer idServicio, String referencia, BigDecimal monto) {
        String montoStr = monto != null ? monto.setScale(2, RoundingMode.HALF_UP).toPlainString() : "0.00";
        String raw = (numeroCuenta != null ? numeroCuenta.trim() : "") + "|" +
                idProducto + "|" +
                idServicio + "|" +
                (referencia != null ? referencia.trim() : "") + "|" +
                montoStr;

        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(raw.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("Error al inicializar algoritmo SHA-256", e);
        }
    }

    @Transactional(readOnly = true)
    public Optional<Transaccion> validarYObtenerPrevia(String idDocumento, Long cuentaId, String requestHashCalculado) {
        if (idDocumento == null || idDocumento.isBlank()) {
            return Optional.empty();
        }

        Optional<Transaccion> previa = cuentaId != null
                ? transaccionRepository.findByCuentaIdAndIdDocumento(cuentaId, idDocumento.trim())
                : transaccionRepository.findByIdDocumento(idDocumento.trim());

        if (previa.isPresent()) {
            Transaccion tx = previa.get();
            if (!tx.getRequestHash().equals(requestHashCalculado)) {
                log.warn("Conflicto de idempotencia: idDocumento {} ya existe con distinto hash", idDocumento);
                throw new DuplicateRequestException("El IdDocumento especificado ya fue utilizado para otra transacción con parámetros distintos.");
            }
            log.info("Transacción previa encontrada por idDocumento {} en estado {}", idDocumento, tx.getEstado());
        }

        return previa;
    }
}

