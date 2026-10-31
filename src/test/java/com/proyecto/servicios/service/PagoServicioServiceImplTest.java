package com.proyecto.servicios.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.proyecto.servicios.client.GestoPagoSendTxClient;
import com.proyecto.servicios.config.GestoPagoProperties;
import com.proyecto.servicios.config.security.AuthenticatedUser;
import com.proyecto.servicios.entity.mongo.CatProductoDocument;
import com.proyecto.servicios.entity.sf.Cliente;
import com.proyecto.servicios.entity.sf.Cuenta;
import com.proyecto.servicios.entity.sf.Transaccion;
import com.proyecto.servicios.enums.AccionTransaccion;
import com.proyecto.servicios.enums.EstadoTransaccion;
import com.proyecto.servicios.model.PagoServicioRequest;
import com.proyecto.servicios.model.PagoServicioResponse;
import com.proyecto.servicios.model.gestopago.xml.GestoPagoSendTxResponse;
import com.proyecto.servicios.repositorys.sf.TransaccionRepository;
import com.proyecto.servicios.service.Impl.PagoServicioServiceImpl;
import com.proyecto.servicios.service.validation.CatalogoPagoValidator;
import com.proyecto.servicios.service.validation.LimitesPago;
import com.proyecto.servicios.service.validation.MontoValidator;
import com.proyecto.servicios.service.validation.ReglaMonto;
import com.proyecto.servicios.util.AesPayloadCipher;
import com.proyecto.servicios.util.JaxbXmlParserUtil;
import com.proyecto.servicios.util.UpcGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PagoServicioServiceImplTest {

    @Mock
    private PinService pinService;
    @Mock
    private CatalogoPagoValidator catalogoValidator;
    @Mock
    private MontoValidator montoValidator;
    @Mock
    private LimitesPago limites;
    @Mock
    private IdempotenciaService idempotenciaService;
    @Mock
    private GestoPagoTokenProvider tokenProvider;
    @Mock
    private PagoTxService pagoTxService;
    @Mock
    private UpcGenerator upcGenerator;
    @Mock
    private AesPayloadCipher cipher;
    @Mock
    private GestoPagoSendTxClient sendTxClient;
    @Mock
    private JaxbXmlParserUtil xmlParser;
    @Mock
    private GestoPagoRespuestaClassifier classifier;
    @Mock
    private TransaccionRepository transaccionRepository;
    @Mock
    private GestoPagoProperties properties;
    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private PagoServicioServiceImpl pagoServicioService;

    private AuthenticatedUser authUser;
    private PagoServicioRequest request;
    private CatProductoDocument productoMock;
    private Transaccion transaccionMock;
    private Cuenta cuentaMock;

    @BeforeEach
    void setUp() {
        authUser = new AuthenticatedUser(
                1L, 1001L, "cliente@banco.com", "hash", "ROLE_CLIENTE", Collections.emptyList(), true);

        request = new PagoServicioRequest(
                "9080512384", 5, 101, "DOC-12345678", "CONTRATO-5555", new BigDecimal("150.00"), "1234");

        productoMock = CatProductoDocument.builder()
                .idProducto(101)
                .idServicio(5)
                .tipoFront(2)
                .precio("12.00")
                .activo(true)
                .build();

        Cliente cliente = Cliente.builder().id(1001L).build();
        cuentaMock = Cuenta.builder().id(10L).numeroCuenta("9080512384").cliente(cliente).build();

        transaccionMock = Transaccion.builder()
                .id(UUID.randomUUID())
                .cuenta(cuentaMock)
                .idDocumento("DOC-12345678")
                .upc("U000000100001")
                .horaLocal("20261028143000")
                .idProducto(101)
                .idServicio(5)
                .referenciaEnmascarada("****5555")
                .monto(new BigDecimal("150.00"))
                .estado(EstadoTransaccion.PENDIENTE)
                .fechaCreacion(OffsetDateTime.now())
                .build();
    }

    @Test
    @DisplayName("Flujo exitoso: respuesta '01' de Gestopago confirma débito y retorna estado EXITOSA")
    void testPagarExitoso() throws Exception {
        when(catalogoValidator.validar(101, 5, "CONTRATO-5555")).thenReturn(productoMock);
        when(montoValidator.resolver(any(ReglaMonto.class), eq(new BigDecimal("150.00")))).thenReturn(new BigDecimal("150.00"));
        when(idempotenciaService.calcularRequestHash(anyString(), anyInt(), anyInt(), anyString(), any(BigDecimal.class))).thenReturn("fakeHash");
        when(idempotenciaService.validarYObtenerPrevia(eq("DOC-12345678"), isNull(), eq("fakeHash"))).thenReturn(Optional.empty());
        when(upcGenerator.generarUpc()).thenReturn("U000000100001");
        when(pagoTxService.reservarFondos(any(), any(), any(), anyString(), anyString(), anyString())).thenReturn(transaccionMock);
        when(objectMapper.writeValueAsString(any())).thenReturn("{\"json\":true}");
        when(cipher.cifrarJson(anyString())).thenReturn("payloadCifradoBase64");
        when(sendTxClient.sendTxXml(any())).thenReturn("<RESPONSE><MENSAJE><CODIGO>01</CODIGO></MENSAJE></RESPONSE>");

        GestoPagoSendTxResponse respGp = new GestoPagoSendTxResponse();
        respGp.setNumAutorizacion("AUT-998877");
        respGp.setIdTx("TX-123");
        GestoPagoSendTxResponse.MensajeNode node = new GestoPagoSendTxResponse.MensajeNode();
        node.setCodigo("01");
        node.setTexto("Operacion exitosa");
        respGp.setMensaje(node);

        when(xmlParser.parseXmlString(anyString(), eq(GestoPagoSendTxResponse.class))).thenReturn(respGp);
        when(classifier.clasificarSendTx(respGp)).thenReturn(AccionTransaccion.CONFIRMAR_EXITO);

        PagoServicioResponse respuesta = pagoServicioService.pagar(request, authUser);

        assertNotNull(respuesta);
        assertEquals(EstadoTransaccion.EXITOSA, respuesta.estado());
        assertEquals("AUT-998877", respuesta.numAutorizacion());
        verify(pagoTxService, times(1)).marcarEnviada(eq(transaccionMock.getId()), eq("payloadCifradoBase64"));
        verify(pagoTxService, times(1)).confirmarExitosa(eq(transaccionMock.getId()), eq("AUT-998877"), eq("TX-123"), eq("01"));
        verify(pagoTxService, never()).reversarTransaccion(any(), any());
    }

    @Test
    @DisplayName("Rechazo definitivo: código '02' (saldo mayorista insuficiente) reversa el saldo retenido")
    void testPagarRechazoDefinitivo() throws Exception {
        when(catalogoValidator.validar(101, 5, "CONTRATO-5555")).thenReturn(productoMock);
        when(montoValidator.resolver(any(ReglaMonto.class), eq(new BigDecimal("150.00")))).thenReturn(new BigDecimal("150.00"));
        when(idempotenciaService.calcularRequestHash(anyString(), anyInt(), anyInt(), anyString(), any(BigDecimal.class))).thenReturn("fakeHash");
        when(idempotenciaService.validarYObtenerPrevia(eq("DOC-12345678"), isNull(), eq("fakeHash"))).thenReturn(Optional.empty());
        when(upcGenerator.generarUpc()).thenReturn("U000000100001");
        when(pagoTxService.reservarFondos(any(), any(), any(), anyString(), anyString(), anyString())).thenReturn(transaccionMock);
        when(objectMapper.writeValueAsString(any())).thenReturn("{\"json\":true}");
        when(cipher.cifrarJson(anyString())).thenReturn("payloadCifradoBase64");
        when(sendTxClient.sendTxXml(any())).thenReturn("<RESPONSE><MENSAJE><CODIGO>02</CODIGO></MENSAJE></RESPONSE>");

        GestoPagoSendTxResponse respGp = new GestoPagoSendTxResponse();
        GestoPagoSendTxResponse.MensajeNode node = new GestoPagoSendTxResponse.MensajeNode();
        node.setCodigo("02");
        node.setTexto("Saldo insuficiente de la distribuidora");
        respGp.setMensaje(node);

        when(xmlParser.parseXmlString(anyString(), eq(GestoPagoSendTxResponse.class))).thenReturn(respGp);
        when(classifier.clasificarSendTx(respGp)).thenReturn(AccionTransaccion.REVERSAR_SALDO);

        PagoServicioResponse respuesta = pagoServicioService.pagar(request, authUser);

        assertNotNull(respuesta);
        assertEquals(EstadoTransaccion.REVERSADA, respuesta.estado());
        verify(pagoTxService, times(1)).reversarTransaccion(eq(transaccionMock.getId()), contains("Saldo insuficiente"));
        verify(pagoTxService, never()).confirmarExitosa(any(), any(), any(), any());
    }

    @Test
    @DisplayName("Timeout o caída de red: marca la transacción como INCIERTA, los fondos SIGUEN retenidos y no se reintenta")
    void testPagarTimeoutRed() throws Exception {
        when(catalogoValidator.validar(101, 5, "CONTRATO-5555")).thenReturn(productoMock);
        when(montoValidator.resolver(any(ReglaMonto.class), eq(new BigDecimal("150.00")))).thenReturn(new BigDecimal("150.00"));
        when(idempotenciaService.calcularRequestHash(anyString(), anyInt(), anyInt(), anyString(), any(BigDecimal.class))).thenReturn("fakeHash");
        when(idempotenciaService.validarYObtenerPrevia(eq("DOC-12345678"), isNull(), eq("fakeHash"))).thenReturn(Optional.empty());
        when(upcGenerator.generarUpc()).thenReturn("U000000100001");
        when(pagoTxService.reservarFondos(any(), any(), any(), anyString(), anyString(), anyString())).thenReturn(transaccionMock);
        when(objectMapper.writeValueAsString(any())).thenReturn("{\"json\":true}");
        when(cipher.cifrarJson(anyString())).thenReturn("payloadCifradoBase64");
        when(sendTxClient.sendTxXml(any())).thenThrow(new RuntimeException("Read timed out (65s)"));

        PagoServicioResponse respuesta = pagoServicioService.pagar(request, authUser);

        assertNotNull(respuesta);
        assertEquals(EstadoTransaccion.PENDIENTE, respuesta.estado()); // En proceso
        verify(pagoTxService, times(1)).marcarIncierta(eq(transaccionMock.getId()), eq("TIMEOUT_RED"), anyString());
        verify(pagoTxService, never()).reversarTransaccion(any(), any());
        verify(pagoTxService, never()).confirmarExitosa(any(), any(), any(), any());
    }

    @Test
    @DisplayName("Idempotencia: Solicitud repetida con mismo IdDocumento devuelve el resultado guardado sin tocar Gestopago")
    void testPagarIdempotenciaPrevia() {
        transaccionMock.setEstado(EstadoTransaccion.EXITOSA);
        transaccionMock.setAutorizacionProveedor("AUT-PREVIA-123");
        transaccionMock.setIdTicketProveedor("TX-PREVIA-456");

        when(catalogoValidator.validar(101, 5, "CONTRATO-5555")).thenReturn(productoMock);
        when(montoValidator.resolver(any(ReglaMonto.class), eq(new BigDecimal("150.00")))).thenReturn(new BigDecimal("150.00"));
        when(idempotenciaService.calcularRequestHash(anyString(), anyInt(), anyInt(), anyString(), any(BigDecimal.class))).thenReturn("fakeHash");
        when(idempotenciaService.validarYObtenerPrevia(eq("DOC-12345678"), isNull(), eq("fakeHash"))).thenReturn(Optional.of(transaccionMock));

        PagoServicioResponse respuesta = pagoServicioService.pagar(request, authUser);

        assertNotNull(respuesta);
        assertEquals(EstadoTransaccion.EXITOSA, respuesta.estado());
        assertEquals("AUT-PREVIA-123", respuesta.numAutorizacion());
        verify(sendTxClient, never()).sendTxXml(any());
        verify(pagoTxService, never()).reservarFondos(any(), any(), any(), any(), any(), any());
    }
}

