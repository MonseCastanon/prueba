package com.proyecto.servicios.config;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;

@Validated
@ConfigurationProperties(prefix = "gestopago")
public record GestoPagoProperties(
        @NotBlank String apiBaseUrl,
        @NotNull Auth auth,
        Crypto crypto,
        String unidad,
        Endpoints endpoints,
        Timeouts timeouts
) {
    public GestoPagoProperties {
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

    public record Auth(
            @NotNull Integer idDistribuidor,
            @NotBlank String codigoDispositivo,
            @NotBlank String password
    ) {
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

