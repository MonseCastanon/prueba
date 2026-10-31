package com.proyecto.servicios.service;

import com.proyecto.servicios.enums.AccionTransaccion;
import com.proyecto.servicios.model.gestopago.xml.GestoPagoConfirmTxResponse;
import com.proyecto.servicios.model.gestopago.xml.GestoPagoSendTxResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GestoPagoRespuestaClassifierTest {

    private GestoPagoRespuestaClassifier classifier;

    @BeforeEach
    void setUp() {
        classifier = new GestoPagoRespuestaClassifier();
    }

    @Test
    @DisplayName("sendTx: Código '01' o '1' clasifica como CONFIRMAR_EXITO")
    void testSendTxExito() {
        GestoPagoSendTxResponse resp1 = crearSendTxResp("01", "Operación exitosa");
        assertEquals(AccionTransaccion.CONFIRMAR_EXITO, classifier.clasificarSendTx(resp1));

        GestoPagoSendTxResponse resp2 = crearSendTxResp("1", "Operación exitosa");
        assertEquals(AccionTransaccion.CONFIRMAR_EXITO, classifier.clasificarSendTx(resp2));
    }

    @Test
    @DisplayName("sendTx: Código '02' (saldo insuficiente de distribuidor en Gestopago) clasifica como REVERSAR_SALDO")
    void testSendTxSaldoDistribuidorInsuficiente() {
        GestoPagoSendTxResponse resp = crearSendTxResp("02", "Saldo insuficiente en cuenta mayorista");
        assertEquals(AccionTransaccion.REVERSAR_SALDO, classifier.clasificarSendTx(resp));
    }

    @Test
    @DisplayName("sendTx: Código '06' (duplicado) y '82' (timeout de switch) clasifican como MARCAR_INCIERTA_PARA_CONCILIACION")
    void testSendTxDuplicadoYTimeout() {
        GestoPagoSendTxResponse respDuplicado = crearSendTxResp("06", "Transaccion aplicada anteriormente");
        assertEquals(AccionTransaccion.MARCAR_INCIERTA_PARA_CONCILIACION, classifier.clasificarSendTx(respDuplicado));

        GestoPagoSendTxResponse respTimeout = crearSendTxResp("82", "Tiempo de espera alcanzado");
        assertEquals(AccionTransaccion.MARCAR_INCIERTA_PARA_CONCILIACION, classifier.clasificarSendTx(respTimeout));
    }

    @Test
    @DisplayName("sendTx: Código desconocido o respuesta nula clasifica como MARCAR_INCIERTA_PARA_CONCILIACION (prudencia financiera)")
    void testSendTxDesconocidoONulo() {
        assertEquals(AccionTransaccion.MARCAR_INCIERTA_PARA_CONCILIACION, classifier.clasificarSendTx(null));

        GestoPagoSendTxResponse resp = crearSendTxResp("99", "Error desconocido");
        assertEquals(AccionTransaccion.MARCAR_INCIERTA_PARA_CONCILIACION, classifier.clasificarSendTx(resp));
    }

    @Test
    @DisplayName("confirmTx: Código '06' es ÉXITO en confirmTx y clasifica como CONFIRMAR_EXITO")
    void testConfirmTxExito() {
        GestoPagoConfirmTxResponse resp = crearConfirmTxResp("06", "La transaccion fue aplicada exitosamente");
        assertEquals(AccionTransaccion.CONFIRMAR_EXITO, classifier.clasificarConfirmTx(resp));
    }

    @Test
    @DisplayName("confirmTx: Código '70' (no aplicada) clasifica como REVERSAR_SALDO")
    void testConfirmTxNoAplicada() {
        GestoPagoConfirmTxResponse resp = crearConfirmTxResp("70", "La transaccion NO fue aplicada");
        assertEquals(AccionTransaccion.REVERSAR_SALDO, classifier.clasificarConfirmTx(resp));
    }

    @Test
    @DisplayName("confirmTx: Código desconocido o nulo clasifica como MARCAR_INCIERTA_PARA_CONCILIACION")
    void testConfirmTxDesconocidoONulo() {
        assertEquals(AccionTransaccion.MARCAR_INCIERTA_PARA_CONCILIACION, classifier.clasificarConfirmTx(null));

        GestoPagoConfirmTxResponse resp = crearConfirmTxResp("50", "Respuesta indeterminada");
        assertEquals(AccionTransaccion.MARCAR_INCIERTA_PARA_CONCILIACION, classifier.clasificarConfirmTx(resp));
    }

    private GestoPagoSendTxResponse crearSendTxResp(String codigo, String texto) {
        GestoPagoSendTxResponse resp = new GestoPagoSendTxResponse();
        GestoPagoSendTxResponse.MensajeNode node = new GestoPagoSendTxResponse.MensajeNode();
        node.setCodigo(codigo);
        node.setTexto(texto);
        resp.setMensaje(node);
        return resp;
    }

    private GestoPagoConfirmTxResponse crearConfirmTxResp(String codigo, String texto) {
        GestoPagoConfirmTxResponse resp = new GestoPagoConfirmTxResponse();
        GestoPagoConfirmTxResponse.MensajeNode node = new GestoPagoConfirmTxResponse.MensajeNode();
        node.setCodigo(codigo);
        node.setTexto(texto);
        resp.setMensaje(node);
        return resp;
    }
}

