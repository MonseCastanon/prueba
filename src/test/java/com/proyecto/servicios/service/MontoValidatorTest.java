package com.proyecto.servicios.service;

import com.proyecto.servicios.entity.mongo.CatProductoDocument;
import com.proyecto.servicios.exception.InvalidPaymentException;
import com.proyecto.servicios.service.validation.LimitesPago;
import com.proyecto.servicios.service.validation.MontoValidator;
import com.proyecto.servicios.service.validation.ReglaMonto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class MontoValidatorTest {

    private MontoValidator montoValidator;
    private LimitesPago limites;

    @BeforeEach
    void setUp() {
        montoValidator = new MontoValidator();
        limites = new LimitesPago(
                new BigDecimal("10.00"),
                new BigDecimal("10000.00"),
                new BigDecimal("50000.00")
        );
    }

    @Test
    @DisplayName("tipoFront=1: Precio fijo del catálogo se resuelve exitosamente cuando el cliente envía el mismo monto")
    void testPrecioFijoExitoso() {
        CatProductoDocument prod = CatProductoDocument.builder()
                .tipoFront(1)
                .precio("50.00")
                .build();

        ReglaMonto regla = ReglaMonto.desde(prod, limites);
        BigDecimal res = montoValidator.resolver(regla, new BigDecimal("50.00"));

        assertEquals(new BigDecimal("50.00"), res);
    }

    @Test
    @DisplayName("tipoFront=1: Precio fijo del catálogo se utiliza si el cliente no envía monto")
    void testPrecioFijoClienteNull() {
        CatProductoDocument prod = CatProductoDocument.builder()
                .tipoFront(1)
                .precio("100.00")
                .build();

        ReglaMonto regla = ReglaMonto.desde(prod, limites);
        BigDecimal res = montoValidator.resolver(regla, null);

        assertEquals(new BigDecimal("100.00"), res);
    }

    @Test
    @DisplayName("tipoFront=1: Falla si el cliente envía un monto distinto al precio fijo del catálogo")
    void testPrecioFijoMontoDistinto() {
        CatProductoDocument prod = CatProductoDocument.builder()
                .tipoFront(1)
                .precio("50.00")
                .build();

        ReglaMonto regla = ReglaMonto.desde(prod, limites);
        assertThrows(InvalidPaymentException.class, () -> montoValidator.resolver(regla, new BigDecimal("30.00")));
    }

    @Test
    @DisplayName("tipoFront=2: Monto libre dentro del rango permitido es aceptado")
    void testMontoLibreExitoso() {
        CatProductoDocument prod = CatProductoDocument.builder()
                .tipoFront(2)
                .precio("12.50") // En tipoFront=2 esto es la comisión del distribuidor
                .build();

        ReglaMonto regla = ReglaMonto.desde(prod, limites);
        BigDecimal res = montoValidator.resolver(regla, new BigDecimal("250.75"));

        assertEquals(new BigDecimal("250.75"), res);
    }

    @Test
    @DisplayName("tipoFront=2: Falla si el monto es nulo, cero o negativo")
    void testMontoLibreInvalido() {
        CatProductoDocument prod = CatProductoDocument.builder()
                .tipoFront(2)
                .build();

        ReglaMonto regla = ReglaMonto.desde(prod, limites);

        assertThrows(InvalidPaymentException.class, () -> montoValidator.resolver(regla, null));
        assertThrows(InvalidPaymentException.class, () -> montoValidator.resolver(regla, BigDecimal.ZERO));
        assertThrows(InvalidPaymentException.class, () -> montoValidator.resolver(regla, new BigDecimal("-10.00")));
    }

    @Test
    @DisplayName("tipoFront=2: Falla si el monto está por debajo del mínimo o por encima del máximo")
    void testMontoLibreFueraDeRango() {
        CatProductoDocument prod = CatProductoDocument.builder()
                .tipoFront(2)
                .build();

        ReglaMonto regla = ReglaMonto.desde(prod, limites);

        assertThrows(InvalidPaymentException.class, () -> montoValidator.resolver(regla, new BigDecimal("5.00"))); // < 10.00
        assertThrows(InvalidPaymentException.class, () -> montoValidator.resolver(regla, new BigDecimal("15000.00"))); // > 10000.00
    }

    @Test
    @DisplayName("Falla si el monto tiene más de dos decimales")
    void testMontoMasDeDosDecimales() {
        CatProductoDocument prod = CatProductoDocument.builder()
                .tipoFront(2)
                .build();

        ReglaMonto regla = ReglaMonto.desde(prod, limites);
        assertThrows(InvalidPaymentException.class, () -> montoValidator.resolver(regla, new BigDecimal("100.125")));
    }

    @Test
    @DisplayName("Falla si el tipoFront no es soportado (ej. 4)")
    void testTipoFrontNoSoportado() {
        CatProductoDocument prod = CatProductoDocument.builder()
                .tipoFront(4)
                .build();

        assertThrows(InvalidPaymentException.class, () -> ReglaMonto.desde(prod, limites));
    }
}

