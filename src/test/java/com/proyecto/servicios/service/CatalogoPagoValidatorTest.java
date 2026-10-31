package com.proyecto.servicios.service;

import com.proyecto.servicios.entity.mongo.CatProductoDocument;
import com.proyecto.servicios.exception.InvalidPaymentException;
import com.proyecto.servicios.repositorys.mongo.CatProductoMongoRepository;
import com.proyecto.servicios.service.validation.CatalogoPagoValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CatalogoPagoValidatorTest {

    @Mock
    private CatProductoMongoRepository mongoRepository;

    @InjectMocks
    private CatalogoPagoValidator validator;

    private CatProductoDocument productoActivo;

    @BeforeEach
    void setUp() {
        productoActivo = CatProductoDocument.builder()
                .idProducto(101)
                .idServicio(5)
                .servicio("CFE")
                .producto("Pago CFE Nacional")
                .tipoFront(2)
                .tipoReferencia("b")
                .activo(true)
                .precio("12.00")
                .build();
    }

    @Test
    @DisplayName("Validación exitosa cuando producto y servicio existen, coinciden y la referencia cumple el formato")
    void testValidacionExitosa() {
        when(mongoRepository.findByIdProducto(101)).thenReturn(Optional.of(productoActivo));

        CatProductoDocument res = validator.validar(101, 5, "CONTRATO-12345");

        assertNotNull(res);
        assertEquals(101, res.getIdProducto());
    }

    @Test
    @DisplayName("Falla si el producto no existe en el catálogo de Mongo")
    void testProductoNoEncontrado() {
        when(mongoRepository.findByIdProducto(999)).thenReturn(Optional.empty());

        assertThrows(InvalidPaymentException.class, () -> validator.validar(999, 5, "12345"));
    }

    @Test
    @DisplayName("Falla si el producto existe pero está inactivo")
    void testProductoInactivo() {
        productoActivo.setActivo(false);
        when(mongoRepository.findByIdProducto(101)).thenReturn(Optional.of(productoActivo));

        assertThrows(InvalidPaymentException.class, () -> validator.validar(101, 5, "12345"));
    }

    @Test
    @DisplayName("Falla si el idServicio no corresponde al configurado en el producto")
    void testServicioNoCorresponde() {
        when(mongoRepository.findByIdProducto(101)).thenReturn(Optional.of(productoActivo));

        // Enviando idServicio = 99 en vez de 5
        assertThrows(InvalidPaymentException.class, () -> validator.validar(101, 99, "12345"));
    }

    @Test
    @DisplayName("Falla si el tipoFront no es 1 ni 2 (ej. tipoFront=30 de cash-in)")
    void testTipoFrontInvalido() {
        productoActivo.setTipoFront(30);
        when(mongoRepository.findByIdProducto(101)).thenReturn(Optional.of(productoActivo));

        assertThrows(InvalidPaymentException.class, () -> validator.validar(101, 5, "12345"));
    }

    @Test
    @DisplayName("Validación de referencia tipo 'a' (teléfono): acepta 10 dígitos y rechaza otros")
    void testTipoReferenciaTelefono() {
        productoActivo.setTipoReferencia("a");
        when(mongoRepository.findByIdProducto(101)).thenReturn(Optional.of(productoActivo));

        assertDoesNotThrow(() -> validator.validar(101, 5, "5512345678"));
        assertThrows(InvalidPaymentException.class, () -> validator.validar(101, 5, "55123")); // Menos de 10
        assertThrows(InvalidPaymentException.class, () -> validator.validar(101, 5, "551234567A")); // Con letras
    }

    @Test
    @DisplayName("Validación de referencia tipo 'c' (código de barras numérico): rechaza letras")
    void testTipoReferenciaCodigoBarras() {
        productoActivo.setTipoReferencia("c");
        when(mongoRepository.findByIdProducto(101)).thenReturn(Optional.of(productoActivo));

        assertDoesNotThrow(() -> validator.validar(101, 5, "7501031311309"));
        assertThrows(InvalidPaymentException.class, () -> validator.validar(101, 5, "BARCODE-XYZ"));
    }
}

