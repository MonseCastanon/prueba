package com.proyecto.servicios.service.Impl;

import com.proyecto.servicios.client.GestoPagoAuthClient;
import com.proyecto.servicios.config.GestoPagoProperties;
import com.proyecto.servicios.entity.gestopago.GestoPagoToken;
import com.proyecto.servicios.exception.ProviderUnavailableException;
import com.proyecto.servicios.mapper.GestoPagoTokenMapper;
import com.proyecto.servicios.model.gestopago.GestoPagoAuthResponse;
import com.proyecto.servicios.repositorys.gestopago.GestoPagoTokenRepository;
import com.proyecto.servicios.service.GestoPagoTokenProvider;
import com.proyecto.servicios.util.AesPayloadCipher;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantLock;

@Slf4j
@Service
public class GestoPagoTokenProviderImpl implements GestoPagoTokenProvider {

    private record CachedToken(String token, OffsetDateTime expiraEn) {
        boolean esValido() {
            return token != null && !token.isBlank() && expiraEn != null && OffsetDateTime.now().isBefore(expiraEn);
        }
    }

    private final GestoPagoAuthClient authClient;
    private final GestoPagoTokenRepository tokenRepository;
    private final GestoPagoTokenMapper tokenMapper;
    private final GestoPagoProperties properties;
    private final AesPayloadCipher cipher;

    private final AtomicReference<CachedToken> tokenCache = new AtomicReference<>();
    private final ReentrantLock renewalLock = new ReentrantLock();

    public GestoPagoTokenProviderImpl(GestoPagoAuthClient authClient,
                                      GestoPagoTokenRepository tokenRepository,
                                      GestoPagoTokenMapper tokenMapper,
                                      GestoPagoProperties properties,
                                      AesPayloadCipher cipher) {
        this.authClient = authClient;
        this.tokenRepository = tokenRepository;
        this.tokenMapper = tokenMapper;
        this.properties = properties;
        this.cipher = cipher;
    }

    @Override
    public String tokenVigente() {
        CachedToken actual = tokenCache.get();
        if (actual != null && actual.esValido()) {
            return actual.token();
        }

        renewalLock.lock();
        try {
            // Verificación doble tras adquirir el cerrojo (Single-flight)
            actual = tokenCache.get();
            if (actual != null && actual.esValido()) {
                return actual.token();
            }

            Integer distId = properties.auth().idDistribuidor();
            String dispCod = properties.auth().codigoDispositivo();

            // 1. Intentar recuperar desde BD si no ha expirado
            Optional<GestoPagoToken> tokenBd = tokenRepository.findByIdDistribuidorAndCodigoDispositivo(distId, dispCod);
            if (tokenBd.isPresent() && tokenBd.get().getToken() != null) {
                GestoPagoToken entity = tokenBd.get();
                // Si la fecha de expiración en BD es válida (o asumimos 23h tras su última actualización)
                OffsetDateTime fechaLimite = entity.getFechaActualizacion() != null
                        ? entity.getFechaActualizacion().atZone(java.time.ZoneId.systemDefault()).toOffsetDateTime().plusHours(23)
                        : OffsetDateTime.now().plusHours(1);

                if (OffsetDateTime.now().isBefore(fechaLimite)) {
                    String tokenClaro = desencriptarSiAplica(entity.getToken());
                    CachedToken nuevoCache = new CachedToken(tokenClaro, fechaLimite);
                    tokenCache.set(nuevoCache);
                    return nuevoCache.token();
                }
            }

            // 2. Renovar contra Gestopago
            log.info("Renovando token JWT de Gestopago (ciclo de 24 horas) para distribuidor={}", distId);
            MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
            form.add("idDistribuidor", String.valueOf(distId));
            form.add("codigoDispositivo", dispCod);
            form.add("password", properties.auth().password());

            GestoPagoAuthResponse response = authClient.authenticate(form);
            if (response == null || response.getToken() == null || response.getToken().isBlank()) {
                log.error("La respuesta de autenticación de Gestopago no retornó un token válido");
                throw new ProviderUnavailableException("No fue posible obtener token de autenticación de Gestopago");
            }

            // Vigencia de 24 horas menos margen de seguridad de 30 minutos = 23.5 horas
            OffsetDateTime expiracion = OffsetDateTime.now().plusHours(23).plusMinutes(30);

            // Guardar en BD cifrado en reposo (AES-256-CBC)
            GestoPagoToken entity = tokenBd.orElseGet(() -> {
                GestoPagoToken t = tokenMapper.toEntity(response);
                t.setIdDistribuidor(distId);
                t.setCodigoDispositivo(dispCod);
                t.setActivo(true);
                return t;
            });
            tokenMapper.updateEntity(response, entity);
            entity.setToken("ENC:" + cipher.cifrarJson(response.getToken()));
            tokenRepository.save(entity);

            CachedToken nuevo = new CachedToken(response.getToken(), expiracion);
            tokenCache.set(nuevo);

            log.info("Token JWT de Gestopago renovado exitosamente. Expira aproximadamente en: {}", expiracion);
            return nuevo.token();

        } catch (Exception e) {
            log.error("Error al autenticar con Gestopago: {}", e.getMessage());
            throw new ProviderUnavailableException("Fallo al autenticar con el switch del proveedor externo: " + e.getMessage(), e);
        } finally {
            renewalLock.unlock();
        }
    }

    @Override
    public void asegurarTokenVigente() {
        tokenVigente();
    }

    @Override
    public void invalidarToken() {
        log.info("Invalidando token JWT de Gestopago en caché");
        tokenCache.set(null);
    }

    private String desencriptarSiAplica(String valorAlmacenado) {
        if (valorAlmacenado != null && valorAlmacenado.startsWith("ENC:")) {
            return cipher.descifrarJson(valorAlmacenado.substring(4));
        }
        return valorAlmacenado;
    }
}
