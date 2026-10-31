package com.proyecto.servicios.service.Impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.proyecto.servicios.client.GestoPagoSendTxClient;
import com.proyecto.servicios.config.GestoPagoProperties;
import com.proyecto.servicios.config.security.AuthenticatedUser;
import com.proyecto.servicios.entity.mongo.CatProductoDocument;
import com.proyecto.servicios.entity.sf.Transaccion;
import com.proyecto.servicios.enums.AccionTransaccion;
import com.proyecto.servicios.exception.ResourceNotFoundException;
import com.proyecto.servicios.model.PagoServicioConsultaRequest;
import com.proyecto.servicios.model.PagoServicioRequest;
import com.proyecto.servicios.model.PagoServicioResponse;
import com.proyecto.servicios.model.gestopago.xml.GestoPagoSendTxResponse;
import com.proyecto.servicios.repositorys.sf.TransaccionRepository;
import com.proyecto.servicios.service.*;
import com.proyecto.servicios.service.validation.CatalogoPagoValidator;
import com.proyecto.servicios.service.validation.LimitesPago;
import com.proyecto.servicios.service.validation.MontoValidator;
import com.proyecto.servicios.service.validation.ReglaMonto;
import com.proyecto.servicios.util.AesPayloadCipher;
import com.proyecto.servicios.util.JaxbXmlParserUtil;
import com.proyecto.servicios.util.UpcGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PagoServicioServiceImpl implements PagoServicioService {

    private final PinService pinService;
    private final CatalogoPagoValidator catalogoValidator;
    private final MontoValidator montoValidator;
    private final LimitesPago limites;
    private final IdempotenciaService idempotenciaService;
    private final GestoPagoTokenProvider tokenProvider;
    private final PagoTxService pagoTxService;
    private final UpcGenerator upcGenerator;
    private final AesPayloadCipher cipher;
    private final GestoPagoSendTxClient sendTxClient;
    private final JaxbXmlParserUtil xmlParser;
    private final GestoPagoRespuestaClassifier classifier;
    private final TransaccionRepository transaccionRepository;
    private final GestoPagoProperties properties;
    private final ObjectMapper objectMapper;

    @Override
    public PagoServicioResponse pagar(PagoServicioRequest req, AuthenticatedUser user) {
        log.info("Iniciando solicitud de pago para cuenta terminación {} y producto {}",
                req.numeroCuenta().substring(Math.max(0, req.numeroCuenta().length() - 4)), req.idProducto());

        // 1. Validaciones fail-fast de seguridad, catálogo y reglas de negocio
        pinService.validarPin(user, req.pin());
        CatProductoDocument prod = catalogoValidator.validar(req.idProducto(), req.idServicio(), req.referencia());
        BigDecimal montoFinal = montoValidator.resolver(ReglaMonto.desde(prod, limites), req.monto());

        // 2. Control estricto de idempotencia por IdDocumento
        String requestHash = idempotenciaService.calcularRequestHash(
                req.numeroCuenta(), req.idProducto(), req.idServicio(), req.referencia(), montoFinal);

        Optional<Transaccion> previa = idempotenciaService.validarYObtenerPrevia(req.idDocumento(), null, requestHash);
        if (previa.isPresent()) {
            return PagoServicioResponse.desdeTransaccion(previa.get());
        }

        // 3. Garantizar disponibilidad de autenticación con el switch ANTES de reservar fondos
        tokenProvider.asegurarTokenVigente();

        // 4. Generación de UPC (3-15 chars) y horaLocal (yyyyMMddHHmmss)
        String upc = upcGenerator.generarUpc();
        String horaLocal = DateTimeFormatter.ofPattern("yyyyMMddHHmmss").format(OffsetDateTime.now());

        // 5. PASO FINANCIERO CRÍTICO: Retención de saldo en transacción de BD corta #1
        Transaccion tx;
        try {
            tx = pagoTxService.reservarFondos(req, montoFinal, user, requestHash, upc, horaLocal);
        } catch (org.springframework.dao.DataIntegrityViolationException dive) {
            Optional<Transaccion> concurrente = idempotenciaService.validarYObtenerPrevia(req.idDocumento(), null, requestHash);
            if (concurrente.isPresent()) {
                return PagoServicioResponse.desdeTransaccion(concurrente.get());
            }
            throw dive;
        }

        // Si reservarFondos detectó bajo lock una transacción previa con el mismo idDocumento (distinto UPC recién generado)
        if (!upc.equals(tx.getUpc())) {
            return PagoServicioResponse.desdeTransaccion(tx);
        }

        // 6. Construir el payload JSON oficial y cifrarlo con AES-256-CBC
        String jsonPayload = construirPayloadJson(tx, prod, horaLocal, upc, montoFinal, req.referencia());
        String payloadCifrado = cipher.cifrarJson(jsonPayload);

        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("random", String.valueOf(System.currentTimeMillis()));
        form.add("signed", payloadCifrado);

        // 7. Marcar como ENVIADA (registra fecha_envio y payload cifrado para confirmTx)
        pagoTxService.marcarEnviada(tx.getId(), payloadCifrado);

        // 8. Llamada HTTP a Gestopago (SIN transacción de BD abierta)
        try {
            log.info("Enviando transacción {} a Gestopago (sendTx.do) con UPC {}", tx.getId(), upc);
            String xmlResponse = sendTxClient.sendTxXml(form);
            GestoPagoSendTxResponse gpResp = xmlParser.parseXmlString(xmlResponse, GestoPagoSendTxResponse.class);

            AccionTransaccion accion = classifier.clasificarSendTx(gpResp);

            return switch (accion) {
                case CONFIRMAR_EXITO -> {
                    pagoTxService.confirmarExitosa(
                            tx.getId(),
                            gpResp.getNumAutorizacion(),
                            gpResp.getIdTx(),
                            gpResp.getMensaje() != null ? gpResp.getMensaje().getCodigo() : "01"
                    );
                    yield PagoServicioResponse.exitosa(tx, gpResp.getNumAutorizacion(), gpResp.getIdTx());
                }
                case REVERSAR_SALDO -> {
                    String motivo = gpResp.getMensaje() != null ? gpResp.getMensaje().getTexto() : "Rechazado por switch";
                    pagoTxService.reversarTransaccion(tx.getId(), motivo);
                    yield PagoServicioResponse.rechazada(tx, motivo);
                }
                case MARCAR_INCIERTA_PARA_CONCILIACION -> {
                    String codigo = gpResp.getMensaje() != null ? gpResp.getMensaje().getCodigo() : "INDETERMINADO";
                    String motivo = gpResp.getMensaje() != null ? gpResp.getMensaje().getTexto() : "Respuesta ambigua";
                    pagoTxService.marcarIncierta(tx.getId(), codigo, motivo);
                    yield PagoServicioResponse.enProceso(tx);
                }
            };

        } catch (Exception ex) {
            // Si el proveedor responde 401/403, invalidar reactivamente el token en caché
            if (ex instanceof feign.FeignException fe && (fe.status() == 401 || fe.status() == 403)) {
                tokenProvider.invalidarToken();
            }
            // ReadTimeoutException, fallo de conexión o error de red
            log.warn("Excepción al comunicar con sendTx de Gestopago para tx {}: {}", tx.getId(), ex.getMessage());
            pagoTxService.marcarIncierta(tx.getId(), "TIMEOUT_RED", ex.getMessage());
            return PagoServicioResponse.enProceso(tx);
        }
    }

    @Override
    public PagoServicioResponse consultar(PagoServicioConsultaRequest request, AuthenticatedUser user) {
        Transaccion tx = transaccionRepository.findByIdDocumento(request.idDocumento().trim())
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró transacción con el IdDocumento: " + request.idDocumento()));

        if (user.clienteId() != null && !user.clienteId().equals(tx.getCuenta().getCliente().getId())) {
            throw new ResourceNotFoundException("No se encontró transacción con el IdDocumento: " + request.idDocumento());
        }

        return PagoServicioResponse.desdeTransaccion(tx);
    }

    private String construirPayloadJson(Transaccion tx, CatProductoDocument prod, String horaLocal, String upc, BigDecimal montoFinal, String referenciaCliente) {
        try {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("idProducto", prod.getIdProducto());
            map.put("idServicio", prod.getIdServicio());

            // Reglas oficiales de teléfono/referencia/montoPago según tipoFront e idCatTipoServicio
            Integer catTipo = prod.getIdCatTipoServicio();
            Integer tipoFront = prod.getTipoFront();

            if (catTipo != null && (catTipo == 10 || catTipo == 11)) {
                // Pines redimibles / entretenimiento
                map.put("telefono", "#1111111111");
            } else if (tipoFront != null && tipoFront == 1) {
                // Recargas telefónicas
                map.put("telefono", referenciaCliente.trim());
            } else if (tipoFront != null && tipoFront == 2) {
                // Pago de servicios
                map.put("telefono", "1111111111");
                map.put("referencia", referenciaCliente.trim());
                map.put("montoPago", montoFinal.doubleValue());
            }

            map.put("horaLocal", horaLocal);
            map.put("upc", upc);
            map.put("unidad", properties.unidad() != null ? properties.unidad() : "083");

            return objectMapper.writeValueAsString(map);
        } catch (Exception e) {
            log.error("Error al serializar payload JSON para Gestopago: {}", e.getMessage());
            throw new IllegalStateException("Error al construir payload para Gestopago", e);
        }
    }
}
