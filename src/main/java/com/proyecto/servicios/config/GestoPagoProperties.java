package com.proyecto.servicios.config;

import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;

@Validated
@ConfigurationProperties(prefix = "gestopago")
public record GestoPagoProperties(
        Api api,
        String apiBaseUrl,
        @NotNull Auth auth,
        Crypto crypto,
        String unidad,
        Endpoints endpoints,
        Timeouts timeouts
) {
    public GestoPagoProperties {
        if (apiBaseUrl == null || apiBaseUrl.isBlank()) {
            if (api != null && api.baseUrl() != null && !api.baseUrl().isBlank()) {
                apiBaseUrl = api.baseUrl();
            } else {
                apiBaseUrl = "https://gestopago.portalventas.net";
            }
        }
        if (unidad == null || unidad.isBlank()) {
            unidad = "083";
        }
        if (endpoints == null) {
            endpoints = new Endpoints(
                    "/sistema/service/sendTx.do",
                    "/sistema/service/confirmTx.do",
                    "/sistema/app/jwt-gp/authenticate/",
                    "/sistema/service/getProductList.do"
            );
        }
        if (timeouts == null) {
            timeouts = new Timeouts(Duration.ofSeconds(5), Duration.ofSeconds(65));
        }
    }

    public record Api(
            String baseUrl,
            String apiKey
    ) {}

    public record Auth(
            @NotNull Integer idDistribuidor,
            String codigoDispositivo,
            String password
    ) {
        public Auth {
            if (codigoDispositivo == null || codigoDispositivo.isBlank()) {
                codigoDispositivo = "GPS83-TPV-17";
            }
            if (password == null || password.isBlank()) {
                password = "defaultPassword123";
            }
        }

        @Override
        public String toString() {
            return "Auth[idDistribuidor=" + idDistribuidor + ", codigoDispositivo=" + codigoDispositivo + ", password=***]";
        }
    }

    public record Crypto(
            String aesSecretKey,
            String aesIv,
            String tokenEncryptionKey
    ) {
        @Override
        public String toString() {
            return "Crypto[***]";
        }
    }

    public record Endpoints(
            String sendTx,
            String confirmTx,
            String auth,
            String getProductList
    ) {}

    public record Timeouts(
            Duration connect,
            Duration read
    ) {}
}
