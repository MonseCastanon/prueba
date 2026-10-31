package com.proyecto.servicios.service;

import com.proyecto.servicios.enums.EstadoTransaccion;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EstadoTransaccionTest {

    @Test
    @DisplayName("Verifica transiciones legales desde PENDIENTE")
    void testTransicionesPendiente() {
        assertTrue(EstadoTransaccion.PENDIENTE.puedePasarA(EstadoTransaccion.ENVIADA));
        assertTrue(EstadoTransaccion.PENDIENTE.puedePasarA(EstadoTransaccion.REVERSADA));
        assertTrue(EstadoTransaccion.PENDIENTE.puedePasarA(EstadoTransaccion.RECHAZADA));
        assertFalse(EstadoTransaccion.PENDIENTE.puedePasarA(EstadoTransaccion.EXITOSA));
    }

    @Test
    @DisplayName("Verifica transiciones legales desde ENVIADA")
    void testTransicionesEnviada() {
        assertTrue(EstadoTransaccion.ENVIADA.puedePasarA(EstadoTransaccion.EXITOSA));
        assertTrue(EstadoTransaccion.ENVIADA.puedePasarA(EstadoTransaccion.REVERSADA));
        assertTrue(EstadoTransaccion.ENVIADA.puedePasarA(EstadoTransaccion.INCIERTA));
        assertFalse(EstadoTransaccion.ENVIADA.puedePasarA(EstadoTransaccion.PENDIENTE));
    }

    @Test
    @DisplayName("Verifica transiciones legales desde INCIERTA")
    void testTransicionesIncierta() {
        assertTrue(EstadoTransaccion.INCIERTA.puedePasarA(EstadoTransaccion.EXITOSA));
        assertTrue(EstadoTransaccion.INCIERTA.puedePasarA(EstadoTransaccion.REVERSADA));
        assertTrue(EstadoTransaccion.INCIERTA.puedePasarA(EstadoTransaccion.REVISION_MANUAL));
        assertFalse(EstadoTransaccion.INCIERTA.puedePasarA(EstadoTransaccion.ENVIADA));
    }

    @Test
    @DisplayName("Estados terminales (EXITOSA, REVERSADA, RECHAZADA) no pueden cambiar de estado")
    void testEstadosTerminales() {
        assertTrue(EstadoTransaccion.EXITOSA.esTerminal());
        assertTrue(EstadoTransaccion.REVERSADA.esTerminal());
        assertTrue(EstadoTransaccion.RECHAZADA.esTerminal());

        assertFalse(EstadoTransaccion.EXITOSA.puedePasarA(EstadoTransaccion.REVERSADA));
        assertFalse(EstadoTransaccion.REVERSADA.puedePasarA(EstadoTransaccion.EXITOSA));
        assertFalse(EstadoTransaccion.RECHAZADA.puedePasarA(EstadoTransaccion.EXITOSA));
    }
}

