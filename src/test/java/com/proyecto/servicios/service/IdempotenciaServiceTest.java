package com.proyecto.servicios.service;

import com.proyecto.servicios.entity.sf.Transaccion;
import com.proyecto.servicios.enums.EstadoTransaccion;
import com.proyecto.servicios.exception.DuplicateRequestException;
import com.proyecto.servicios.repositorys.sf.TransaccionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IdempotenciaServiceTest {

    @Mock
    private TransaccionRepository transaccionRepository;

    @InjectMocks
    private IdempotenciaService idempotenciaService;

    private String hashOriginal;

    @BeforeEach
    void setUp() {
        hashOriginal = idempotenciaService.calcularRequestHash(
                "9080512384", 101, 5, "CONTRATO-12345", new BigDecimal("150.00"));
    }

    @Test
    @DisplayName("Cálculo de hash SHA-256 es determinista y no depende del formato de escala del BigDecimal")
    void testCalculoHashDeterminista() {
        String hash2 = idempotenciaService.calcularRequestHash(
                "9080512384", 101, 5, "CONTRATO-12345", new BigDecimal("150.0"));

        assertEquals(hashOriginal, hash2);
        assertNotNull(hashOriginal);
        assertEquals(64, hashOriginal.length());
    }

    @Test
    @DisplayName("IdDocumento existente con el mismo hash retorna la transacción previa sin error")
    void testValidarPreviaMismoHash() {
        Transaccion txMock = Transaccion.builder()
                .idDocumento("DOC-12345678")
                .requestHash(hashOriginal)
                .estado(EstadoTransaccion.EXITOSA)
                .build();

        when(transaccionRepository.findByIdDocumento("DOC-12345678")).thenReturn(Optional.of(txMock));

        Optional<Transaccion> resultado = idempotenciaService.validarYObtenerPrevia("DOC-12345678", null, hashOriginal);

        assertTrue(resultado.isPresent());
        assertEquals("DOC-12345678", resultado.get().getIdDocumento());
    }

    @Test
    @DisplayName("IdDocumento reutilizado con datos distintos (distinto hash) lanza DuplicateRequestException (HTTP 409)")
    void testValidarPreviaDistintoHash() {
        Transaccion txMock = Transaccion.builder()
                .idDocumento("DOC-12345678")
                .requestHash("hash_completamente_distinto_a_la_solicitud_actual")
                .estado(EstadoTransaccion.EXITOSA)
                .build();

        when(transaccionRepository.findByIdDocumento("DOC-12345678")).thenReturn(Optional.of(txMock));

        assertThrows(DuplicateRequestException.class, () ->
                idempotenciaService.validarYObtenerPrevia("DOC-12345678", null, hashOriginal));
    }
}

