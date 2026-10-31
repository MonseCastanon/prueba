package com.proyecto.servicios.service.Impl;

import com.proyecto.servicios.client.GestoPagoAuthClient;
import com.proyecto.servicios.entity.gestopago.GestoPagoToken;
import com.proyecto.servicios.mapper.GestoPagoTokenMapper;
import com.proyecto.servicios.model.gestopago.GestoPagoAuthResponse;
import com.proyecto.servicios.repositorys.gestopago.GestoPagoTokenRepository;
import com.proyecto.servicios.service.GestoPagoTokenService;
import com.proyecto.servicios.util.AesPayloadCipher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.Optional;

@Service
public class GestoPagoTokenServiceImpl implements GestoPagoTokenService {

    private static final Logger log = LoggerFactory.getLogger(GestoPagoTokenServiceImpl.class);

    private final GestoPagoAuthClient gestoPagoAuthClient;
    private final GestoPagoTokenRepository tokenRepository;
    private final GestoPagoTokenMapper tokenMapper;
    private final AesPayloadCipher cipher;

    @Value("${gestopago.auth.id-distribuidor:83}")
    private Integer idDistribuidor;

    @Value("${gestopago.auth.codigo-dispositivo:GPS83-TPV-17}")
    private String codigoDispositivo;

    @Value("${gestopago.auth.password}")
    private String password;

    public GestoPagoTokenServiceImpl(GestoPagoAuthClient gestoPagoAuthClient,
                                     GestoPagoTokenRepository tokenRepository,
                                     GestoPagoTokenMapper tokenMapper,
                                     AesPayloadCipher cipher) {
        this.gestoPagoAuthClient = gestoPagoAuthClient;
        this.tokenRepository = tokenRepository;
        this.tokenMapper = tokenMapper;
        this.cipher = cipher;
    }

    @Override
    @Scheduled(fixedRateString = "${gestopago.auth.refresh-rate-ms:82800000}", initialDelayString = "${gestopago.auth.initial-delay-ms:60000}")
    public void renovarToken() {
        log.info("Renovando token GestoPago para distribuidor={}, dispositivo={}", idDistribuidor, codigoDispositivo);
        try {
            MultiValueMap<String, String> formParams = new LinkedMultiValueMap<>();
            formParams.add("idDistribuidor", String.valueOf(idDistribuidor));
            formParams.add("codigoDispositivo", codigoDispositivo);
            formParams.add("password", password);

            GestoPagoAuthResponse response = gestoPagoAuthClient.authenticate(formParams);

            if (response == null || response.getToken() == null) {
                log.error("La respuesta de GestoPago no contiene token");
                return;
            }

            GestoPagoToken tokenEntity = tokenRepository
                    .findByIdDistribuidorAndCodigoDispositivo(idDistribuidor, codigoDispositivo)
                    .map(existing -> {
                        tokenMapper.updateEntity(response, existing);
                        return existing;
                    })
                    .orElseGet(() -> {
                        GestoPagoToken nuevo = tokenMapper.toEntity(response);
                        nuevo.setIdDistribuidor(idDistribuidor);
                        nuevo.setCodigoDispositivo(codigoDispositivo);
                        nuevo.setActivo(true);
                        return nuevo;
                    });

            tokenEntity.setToken("ENC:" + cipher.cifrarJson(response.getToken()));
            tokenRepository.save(tokenEntity);
            log.info("Token GestoPago renovado exitosamente para distribuidor={}", idDistribuidor);

        } catch (Exception e) {
            log.error("Error al renovar token GestoPago: {}", e.getMessage(), e);
        }
    }

    @Override
    public Optional<GestoPagoToken> obtenerTokenActivo(Integer idDistribuidor, String codigoDispositivo) {
        Optional<GestoPagoToken> tokenOpt = tokenRepository.findByIdDistribuidorAndCodigoDispositivo(idDistribuidor, codigoDispositivo);
        if (tokenOpt.isEmpty() || tokenOpt.get().getToken() == null) {
            log.info("No se encontró token en BD, intentando renovar inmediatamente...");
            renovarToken();
            tokenOpt = tokenRepository.findByIdDistribuidorAndCodigoDispositivo(idDistribuidor, codigoDispositivo);
        }
        return tokenOpt.map(entity -> {
            if (entity.getToken() != null && entity.getToken().startsWith("ENC:")) {
                GestoPagoToken copy = new GestoPagoToken();
                copy.setId(entity.getId());
                copy.setIdDistribuidor(entity.getIdDistribuidor());
                copy.setCodigoDispositivo(entity.getCodigoDispositivo());
                copy.setToken(cipher.descifrarJson(entity.getToken().substring(4)));
                copy.setActivo(entity.getActivo());
                copy.setFechaCreacion(entity.getFechaCreacion());
                copy.setFechaActualizacion(entity.getFechaActualizacion());
                return copy;
            }
            return entity;
        });
    }
}
