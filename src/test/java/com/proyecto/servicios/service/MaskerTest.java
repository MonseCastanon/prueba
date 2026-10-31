package com.proyecto.servicios.service;

import com.proyecto.servicios.util.Masker;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MaskerTest {

    @Test
    @DisplayName("Enmascaramiento de cuenta: oculta dígitos dejando sólo los últimos 4")
    void testEnmascararCuenta() {
        assertEquals("****1234", Masker.enmascararCuenta("9080511234"));
        assertEquals("****", Masker.enmascararCuenta("1234"));
        assertEquals("****", Masker.enmascararCuenta("12"));
        assertEquals("****", Masker.enmascararCuenta(null));
        assertEquals("****", Masker.enmascararCuenta(""));
    }

    @Test
    @DisplayName("Enmascaramiento de referencia: oculta prefijo dejando sólo los últimos 4 caracteres")
    void testEnmascararReferencia() {
        assertEquals("****5678", Masker.enmascararReferencia("5512345678"));
        assertEquals("****ABCD", Masker.enmascararReferencia("CONTRATO-ABCD"));
        assertEquals("****99", Masker.enmascararReferencia("99"));
        assertEquals("****", Masker.enmascararReferencia(null));
    }

    @Test
    @DisplayName("Enmascaramiento de PIN: oculta el valor completo")
    void testEnmascararPin() {
        assertEquals("****", Masker.enmascararPin("1234"));
        assertEquals("****", Masker.enmascararPin("123456"));
    }
}

