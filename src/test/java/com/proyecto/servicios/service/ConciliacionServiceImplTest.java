package com.proyecto.servicios.service;

import com.proyecto.servicios.client.GestoPagoSendTxClient;
import com.proyecto.servicios.entity.sf.Transaccion;
import com.proyecto.servicios.enums.AccionTransaccion;
import com.proyecto.servicios.enums.EstadoTransaccion;
import com.proyecto.servicios.model.gestopago.xml.GestoPagoConfirmTxResponse;
import com.proyecto.servicios.repositorys.sf.TransaccionRepository;
import com.proyecto.servicios.service.Impl.ConciliacionServiceImpl;
import com.proyecto.servicios.util.JaxbXmlParserUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConciliacionServiceImplTest {

    @Mock
    private GestoPagoSendTxClient sendTxClient;
    @Mock
    private JaxbXmlParserUtil xmlParser;
    @Mock
    private GestoPagoRespuestaClassifier classifier;
    @Mock
    private PagoTxService pagoTxService;
    @Mock
    private TransaccionRepository transaccionRepository;
    @Mock
    private GestoPagoTokenProvider tokenProvider;

    @InjectMocks
    private ConciliacionServiceImpl conciliacionService;

    private Transaccion transaccionIncierta;

    @BeforeEach
    void setUp() {
        transaccionIncierta = Transaccion.builder()
                .id(UUID.randomUUID())
                .upc("U000000100001")
                .estado(EstadoTransaccion.INCIERTA)
                .payloadCifrado("payloadCifradoBase64")
                .fechaEnvio(OffsetDateTime.now().minusSeconds(100)) // Hace 100 segundos (> 62s y < 24h)
                .intentosConciliacion((short) 0)
                .build();
    }

    @Test
    @DisplayName("Regla PuntoRed confirmTx: Respuesta '06' confirma que el pago SÍ fue aplicado")
    void testConciliacionExitosaCodigo06() {
        when(sendTxClient.confirmTxXml(any())).thenReturn("<RESPONSE><MENSAJE><CODIGO>06</CODIGO></MENSAJE></RESPONSE>");

        GestoPagoConfirmTxResponse resp = new GestoPagoConfirmTxResponse();
        resp.setNumAutorizacion("AUT-CONF-8899");
        resp.setIdTx("TX-CONF-7766");
        GestoPagoConfirmTxResponse.MensajeNode node = new GestoPagoConfirmTxResponse.MensajeNode();
        node.setCodigo("06");
        node.setTexto("La transaccion fue aplicada exitosamente");
        resp.setMensaje(node);

        when(xmlParser.parseXmlString(any(), eq(GestoPagoConfirmTxResponse.class))).thenReturn(resp);
        when(classifier.clasificarConfirmTx(resp)).thenReturn(AccionTransaccion.CONFIRMAR_EXITO);

        conciliacionService.conciliarTransaccion(transaccionIncierta);

        verify(pagoTxService, times(1)).confirmarExitosa(eq(transaccionIncierta.getId()), eq("AUT-CONF-8899"), eq("TX-CONF-7766"), eq("06"));
        verify(pagoTxService, never()).reversarTransaccion(any(), any());
    }

    @Test
    @DisplayName("Regla PuntoRed confirmTx: Respuesta '70' confirma que la transacción NO fue aplicada, reversando el saldo")
    void testConciliacionReversoCodigo70() {
        when(sendTxClient.confirmTxXml(any())).thenReturn("<RESPONSE><MENSAJE><CODIGO>70</CODIGO></MENSAJE></RESPONSE>");

        GestoPagoConfirmTxResponse resp = new GestoPagoConfirmTxResponse();
        GestoPagoConfirmTxResponse.MensajeNode node = new GestoPagoConfirmTxResponse.MensajeNode();
        node.setCodigo("70");
        node.setTexto("La transaccion NO fue aplicada");
        resp.setMensaje(node);

        when(xmlParser.parseXmlString(any(), eq(GestoPagoConfirmTxResponse.class))).thenReturn(resp);
        when(classifier.clasificarConfirmTx(resp)).thenReturn(AccionTransaccion.REVERSAR_SALDO);

        conciliacionService.conciliarTransaccion(transaccionIncierta);

        verify(pagoTxService, times(1)).reversarTransaccion(eq(transaccionIncierta.getId()), contains("NO fue aplicada"));
        verify(pagoTxService, never()).confirmarExitosa(any(), any(), any(), any());
    }

    @Test
    @DisplayName("Regla PuntoRed: No ejecutar confirmTx antes de 62 segundos (evita falsos negativos)")
    void testOmitirSiMenorA62Segundos() {
        transaccionIncierta.setFechaEnvio(OffsetDateTime.now().minusSeconds(30)); // 30s (< 62s)

        conciliacionService.conciliarTransaccion(transaccionIncierta);

        verify(sendTxClient, never()).confirmTxXml(any());
        verify(pagoTxService, never()).confirmarExitosa(any(), any(), any(), any());
        verify(pagoTxService, never()).reversarTransaccion(any(), any());
    }

    @Test
    @DisplayName("Regla PuntoRed: Si excede las 24 horas, pasa a REVISION_MANUAL sin reintentar confirmTx")
    void testExpiracionMayorA24Horas() {
        transaccionIncierta.setFechaEnvio(OffsetDateTime.now().minusHours(25)); // 25h (> 24h)

        conciliacionService.conciliarTransaccion(transaccionIncierta);

        assertEquals(EstadoTransaccion.REVISION_MANUAL, transaccionIncierta.getEstado());
        verify(transaccionRepository, times(1)).save(transaccionIncierta);
        verify(sendTxClient, never()).confirmTxXml(any());
    }

    @Test
    @DisplayName("Timeout o fallo en confirmTx incrementa contador de intentos y reprograma")
    void testFalloComunicacionReprograma() {
        when(sendTxClient.confirmTxXml(any())).thenThrow(new RuntimeException("Timeout en confirmTx"));

        conciliacionService.conciliarTransaccion(transaccionIncierta);

        assertEquals((short) 1, transaccionIncierta.getIntentosConciliacion());
        verify(transaccionRepository, times(1)).save(transaccionIncierta);
        verify(pagoTxService, never()).confirmarExitosa(any(), any(), any(), any());
        verify(pagoTxService, never()).reversarTransaccion(any(), any());
    }
}

