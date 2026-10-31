package com.proyecto.servicios.integration;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.proyecto.servicios.client.GestoPagoSendTxClient;
import com.proyecto.servicios.enums.AccionTransaccion;
import com.proyecto.servicios.model.gestopago.xml.GestoPagoConfirmTxResponse;
import com.proyecto.servicios.model.gestopago.xml.GestoPagoSendTxResponse;
import com.proyecto.servicios.service.GestoPagoRespuestaClassifier;
import com.proyecto.servicios.util.JaxbXmlParserUtil;
import feign.Feign;
import feign.Request;
import feign.Retryer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.cloud.openfeign.support.SpringDecoder;
import org.springframework.cloud.openfeign.support.SpringEncoder;
import org.springframework.cloud.openfeign.support.SpringMvcContract;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.concurrent.TimeUnit;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class WireMockGestopagoIntegrationTest {

    private static WireMockServer wireMockServer;
    private static GestoPagoSendTxClient client;
    private static JaxbXmlParserUtil xmlParser;
    private static GestoPagoRespuestaClassifier classifier;

    @BeforeAll
    static void startWireMock() {
        wireMockServer = new WireMockServer(WireMockConfiguration.wireMockConfig().dynamicPort());
        wireMockServer.start();
        WireMock.configureFor("localhost", wireMockServer.port());

        xmlParser = new JaxbXmlParserUtil();
        classifier = new GestoPagoRespuestaClassifier();

        HttpMessageConverters converters = new HttpMessageConverters(
                new FormHttpMessageConverter(),
                new StringHttpMessageConverter()
        );

        client = Feign.builder()
                .contract(new SpringMvcContract())
                .encoder(new SpringEncoder(() -> converters))
                .decoder(new SpringDecoder(() -> converters))
                .retryer(Retryer.NEVER_RETRY)
                .options(new Request.Options(2000, TimeUnit.MILLISECONDS, 3000, TimeUnit.MILLISECONDS, true))
                .target(GestoPagoSendTxClient.class, "http://localhost:" + wireMockServer.port());
    }

    @AfterAll
    static void stopWireMock() {
        if (wireMockServer != null) {
            wireMockServer.stop();
        }
    }

    @Test
    @DisplayName("WireMock: sendTx.do retorna XML exitoso '01' -> Se clasifica como CONFIRMAR_EXITO")
    void testWireMockSendTxExitoso() {
        String xmlExito = """
                <RESPONSE>
                    <ID_TX>291437188</ID_TX>
                    <NUM_AUTORIZACION>100024988</NUM_AUTORIZACION>
                    <SALDO>157731.83</SALDO>
                    <COMISION>0.7</COMISION>
                    <FECHA>28/Oct/2026 14:15:25</FECHA>
                    <MONTO>150.0</MONTO>
                    <MENSAJE>
                        <CODIGO>01</CODIGO>
                        <TEXTO>Operacion realizada con exito</TEXTO>
                        <REFERENCIA></REFERENCIA>
                    </MENSAJE>
                </RESPONSE>
                """;

        wireMockServer.stubFor(post(urlEqualTo("/sistema/service/sendTx.do"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/xml")
                        .withBody(xmlExito)));

        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("random", "1730140000000");
        form.add("signed", "payloadCifradoAES256");

        String responseXml = client.sendTxXml(form);
        assertNotNull(responseXml);

        GestoPagoSendTxResponse parsed = xmlParser.parseXmlString(responseXml, GestoPagoSendTxResponse.class);
        assertEquals("01", parsed.getMensaje().getCodigo());
        assertEquals("100024988", parsed.getNumAutorizacion());
        assertEquals(AccionTransaccion.CONFIRMAR_EXITO, classifier.clasificarSendTx(parsed));
    }

    @Test
    @DisplayName("WireMock: sendTx.do retorna código '02' (saldo mayorista insuficiente) -> Se clasifica como REVERSAR_SALDO")
    void testWireMockSendTxSaldoInsuficiente() {
        String xmlSaldoInsuficiente = """
                <RESPONSE>
                    <ID_TX>-1</ID_TX>
                    <NUM_AUTORIZACION>-1</NUM_AUTORIZACION>
                    <SALDO>0.00</SALDO>
                    <MENSAJE>
                        <CODIGO>02</CODIGO>
                        <TEXTO>Saldo insuficiente en cuenta distribuidor</TEXTO>
                    </MENSAJE>
                </RESPONSE>
                """;

        wireMockServer.stubFor(post(urlEqualTo("/sistema/service/sendTx.do"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/xml")
                        .withBody(xmlSaldoInsuficiente)));

        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("random", "1730140000000");
        form.add("signed", "payloadCifradoAES256");

        String responseXml = client.sendTxXml(form);
        GestoPagoSendTxResponse parsed = xmlParser.parseXmlString(responseXml, GestoPagoSendTxResponse.class);

        assertEquals("02", parsed.getMensaje().getCodigo());
        assertEquals(AccionTransaccion.REVERSAR_SALDO, classifier.clasificarSendTx(parsed));
    }

    @Test
    @DisplayName("WireMock: confirmTx.do retorna código '06' -> Se clasifica como CONFIRMAR_EXITO")
    void testWireMockConfirmTxExitoso() {
        String xmlConfirmExito = """
                <RESPONSE>
                    <ID_TX>291437188</ID_TX>
                    <NUM_AUTORIZACION>100024988</NUM_AUTORIZACION>
                    <MENSAJE>
                        <CODIGO>06</CODIGO>
                        <TEXTO>La transaccion fue aplicada exitosamente</TEXTO>
                    </MENSAJE>
                </RESPONSE>
                """;

        wireMockServer.stubFor(post(urlEqualTo("/sistema/service/confirmTx.do"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/xml")
                        .withBody(xmlConfirmExito)));

        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("random", "1730140000000");
        form.add("signed", "payloadCifradoAES256");

        String responseXml = client.confirmTxXml(form);
        GestoPagoConfirmTxResponse parsed = xmlParser.parseXmlString(responseXml, GestoPagoConfirmTxResponse.class);

        assertEquals("06", parsed.getMensaje().getCodigo());
        assertEquals(AccionTransaccion.CONFIRMAR_EXITO, classifier.clasificarConfirmTx(parsed));
    }

    @Test
    @DisplayName("WireMock: confirmTx.do retorna código '70' -> Se clasifica como REVERSAR_SALDO")
    void testWireMockConfirmTxNoAplicada() {
        String xmlConfirmNoAplicada = """
                <RESPONSE>
                    <ID_TX>-1</ID_TX>
                    <NUM_AUTORIZACION>0</NUM_AUTORIZACION>
                    <MENSAJE>
                        <CODIGO>70</CODIGO>
                        <TEXTO>La transaccion NO fue aplicada</TEXTO>
                    </MENSAJE>
                </RESPONSE>
                """;

        wireMockServer.stubFor(post(urlEqualTo("/sistema/service/confirmTx.do"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/xml")
                        .withBody(xmlConfirmNoAplicada)));

        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("random", "1730140000000");
        form.add("signed", "payloadCifradoAES256");

        String responseXml = client.confirmTxXml(form);
        GestoPagoConfirmTxResponse parsed = xmlParser.parseXmlString(responseXml, GestoPagoConfirmTxResponse.class);

        assertEquals("70", parsed.getMensaje().getCodigo());
        assertEquals(AccionTransaccion.REVERSAR_SALDO, classifier.clasificarConfirmTx(parsed));
    }
}
