package com.proyecto.servicios.controller;

import com.proyecto.servicios.exception.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class GlobalExceptionHandlerTest {

    private MockMvc mockMvc;

    @RestController
    static class TestExceptionController {
        @GetMapping("/test/invalid-payment")
        public void throwInvalidPayment() {
            throw new InvalidPaymentException("Monto fuera de rango");
        }

        @GetMapping("/test/insufficient-funds")
        public void throwInsufficientFunds() {
            throw new InsufficientFundsException("Saldo insuficiente");
        }

        @GetMapping("/test/duplicate-request")
        public void throwDuplicateRequest() {
            throw new DuplicateRequestException("IdDocumento duplicado con diferente hash");
        }

        @GetMapping("/test/pin-blocked")
        public void throwPinBlocked() {
            throw new PinBlockedException("PIN temporalmente bloqueado");
        }

        @GetMapping("/test/too-many-requests")
        public void throwTooManyRequests() {
            throw new TooManyRequestsException("Límite de peticiones alcanzado");
        }

        @GetMapping("/test/external-service")
        public void throwExternalService() {
            throw new ExternalServiceException("Error en gateway de PuntoRed", 503);
        }

        @GetMapping("/test/pessimistic-lock")
        public void throwPessimisticLock() {
            throw new PessimisticLockingFailureException("Timeout al adquirir lock pesimista");
        }

        @GetMapping("/test/data-integrity")
        public void throwDataIntegrity() {
            throw new DataIntegrityViolationException("duplicate key value violates unique constraint uq_tx_upc",
                    new RuntimeException("detalle interno SQL"));
        }

        @GetMapping("/test/illegal-argument")
        public void throwIllegalArgument() {
            throw new IllegalArgumentException("Parámetro interno confidencial");
        }

        @GetMapping("/test/generic-error")
        public void throwGeneric() {
            throw new RuntimeException("Error inesperado de base de datos o nulo");
        }
    }

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new TestExceptionController())
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("InvalidPaymentException -> HTTP 422 UNPROCESSABLE ENTITY")
    void testInvalidPayment() throws Exception {
        mockMvc.perform(get("/test/invalid-payment"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.status").value(422))
                .andExpect(jsonPath("$.mensaje").value("Monto fuera de rango"));
    }

    @Test
    @DisplayName("InsufficientFundsException -> HTTP 422 UNPROCESSABLE ENTITY")
    void testInsufficientFunds() throws Exception {
        mockMvc.perform(get("/test/insufficient-funds"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.status").value(422))
                .andExpect(jsonPath("$.mensaje").value("Saldo insuficiente"));
    }

    @Test
    @DisplayName("DuplicateRequestException -> HTTP 409 CONFLICT")
    void testDuplicateRequest() throws Exception {
        mockMvc.perform(get("/test/duplicate-request"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    @DisplayName("PinBlockedException -> HTTP 423 LOCKED")
    void testPinBlocked() throws Exception {
        mockMvc.perform(get("/test/pin-blocked"))
                .andExpect(status().isLocked())
                .andExpect(jsonPath("$.status").value(423));
    }

    @Test
    @DisplayName("TooManyRequestsException -> HTTP 429 TOO MANY REQUESTS")
    void testTooManyRequests() throws Exception {
        mockMvc.perform(get("/test/too-many-requests"))
                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.status").value(429));
    }

    @Test
    @DisplayName("ExternalServiceException -> HTTP 503 SERVICE UNAVAILABLE sin exponer cuerpo crudo")
    void testExternalService() throws Exception {
        mockMvc.perform(get("/test/external-service"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.status").value(503));
    }

    @Test
    @DisplayName("PessimisticLockingFailureException -> HTTP 409 CONFLICT con mensaje amigable")
    void testPessimisticLocking() throws Exception {
        mockMvc.perform(get("/test/pessimistic-lock"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.mensaje").value("Operación en curso sobre la cuenta bancaria. Por favor reintente."));
    }

    @Test
    @DisplayName("DataIntegrityViolationException -> HTTP 409 CONFLICT sin fugar nombres de constraints ni SQL")
    void testDataIntegrity() throws Exception {
        mockMvc.perform(get("/test/data-integrity"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.mensaje").value("Conflicto con una operación registrada previamente."));
    }

    @Test
    @DisplayName("IllegalArgumentException -> HTTP 400 BAD REQUEST con mensaje sanitizado")
    void testIllegalArgumentSanitizado() throws Exception {
        mockMvc.perform(get("/test/illegal-argument"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.mensaje").value("Solicitud inválida o argumento no admitido"));
    }

    @Test
    @DisplayName("Generic Exception -> HTTP 500 INTERNAL SERVER ERROR con correlationId")
    void testGenericErrorConCorrelationId() throws Exception {
        mockMvc.perform(get("/test/generic-error"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.detalles.correlationId").isNotEmpty());
    }
}

