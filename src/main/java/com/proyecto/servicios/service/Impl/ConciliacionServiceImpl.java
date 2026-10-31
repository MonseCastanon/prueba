package com.proyecto.servicios.service.Impl;

import com.proyecto.servicios.client.GestoPagoSendTxClient;
import com.proyecto.servicios.entity.sf.Transaccion;
import com.proyecto.servicios.enums.AccionTransaccion;
import com.proyecto.servicios.enums.EstadoTransaccion;
import com.proyecto.servicios.model.gestopago.xml.GestoPagoConfirmTxResponse;
import com.proyecto.servicios.repositorys.sf.TransaccionRepository;
import com.proyecto.servicios.service.ConciliacionService;
import com.proyecto.servicios.service.GestoPagoRespuestaClassifier;
import com.proyecto.servicios.service.GestoPagoTokenProvider;
import com.proyecto.servicios.service.PagoTxService;
import com.proyecto.servicios.util.JaxbXmlParserUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.time.Duration;
import java.time.OffsetDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class ConciliacionServiceImpl implements ConciliacionService {

    private final GestoPagoSendTxClient sendTxClient;
    private final JaxbXmlParserUtil xmlParser;
    private final GestoPagoRespuestaClassifier classifier;
    private final PagoTxService pagoTxService;
    private final TransaccionRepository transaccionRepository;
    private final GestoPagoTokenProvider tokenProvider;

    @Override
    public void conciliarTransaccion(Transaccion tx) {
        if (tx == null || tx.getPayloadCifrado() == null || tx.getPayloadCifrado().isBlank()) {
            log.warn("Transacción nula o sin payload cifrado no puede ser conciliada");
            return;
        }

        if (tx.getEstado().esTerminal()) {
            return;
        }

        OffsetDateTime ahora = OffsetDateTime.now();

        // 1. Regla oficial PuntoRed: No ejecutar antes de 62 segundos
        if (tx.getFechaEnvio() != null && Duration.between(tx.getFechaEnvio(), ahora).getSeconds() < 62) {
            log.debug("Transacción {} tiene menos de 62 segundos desde su envío. Se omite para este ciclo.", tx.getId());
            return;
        }

        // 2. Regla oficial PuntoRed: Ventana máxima de 24 horas
        if (tx.getFechaEnvio() != null && Duration.between(tx.getFechaEnvio(), ahora).toHours() >= 24) {
            log.warn("Transacción {} superó la ventana operativa de 24 horas sin resolución. Pasando a REVISION_MANUAL.", tx.getId());
            tx.setEstado(EstadoTransaccion.REVISION_MANUAL);
            tx.setFechaResolucion(ahora);
            transaccionRepository.save(tx);
            return;
        }

        tokenProvider.asegurarTokenVigente();

        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("random", String.valueOf(System.currentTimeMillis()));
        form.add("signed", tx.getPayloadCifrado());

        try {
            log.info("Ejecutando confirmTx.do para conciliar transacción {} con UPC {}", tx.getId(), tx.getUpc());
            String xmlResponse = sendTxClient.confirmTxXml(form);
            GestoPagoConfirmTxResponse resp = xmlParser.parseXmlString(xmlResponse, GestoPagoConfirmTxResponse.class);

            AccionTransaccion accion = classifier.clasificarConfirmTx(resp);

            switch (accion) {
                case CONFIRMAR_EXITO -> {
                    log.info("Conciliación exitosa para transacción {}: el pago fue aplicado en PuntoRed (Código 06)", tx.getId());
                    pagoTxService.confirmarExitosa(
                            tx.getId(),
                            resp.getNumAutorizacion(),
                            resp.getIdTx(),
                            resp.getMensaje() != null ? resp.getMensaje().getCodigo() : "06"
                    );
                }
                case REVERSAR_SALDO -> {
                    String texto = resp.getMensaje() != null ? resp.getMensaje().getTexto() : "Transacción no aplicada (Código 70)";
                    log.info("Conciliación determinó que la transacción {} NO fue aplicada. Reversando fondos.", tx.getId());
                    pagoTxService.reversarTransaccion(tx.getId(), texto);
                }
                case MARCAR_INCIERTA_PARA_CONCILIACION -> {
                    log.warn("confirmTx.do retornó respuesta indeterminada para tx {}. Se reprogramará.", tx.getId());
                    reprogramarIntento(tx);
                }
            }

        } catch (Exception ex) {
            log.warn("Error de comunicación durante confirmTx para transacción {}: {}", tx.getId(), ex.getMessage());
            reprogramarIntento(tx);
        }
    }

    private void reprogramarIntento(Transaccion tx) {
        short intentos = (short) (tx.getIntentosConciliacion() + 1);
        tx.setIntentosConciliacion(intentos);
        // Backoff exponencial simple: 1m, 2m, 3m...
        long segundosEspera = Math.min(300, 60L * intentos);
        tx.setProximoIntentoEn(OffsetDateTime.now().plusSeconds(segundosEspera));
        transaccionRepository.save(tx);
    }
}

