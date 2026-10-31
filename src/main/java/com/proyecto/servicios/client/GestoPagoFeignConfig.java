package com.proyecto.servicios.client;

import com.proyecto.servicios.service.GestoPagoTokenProvider;
import feign.RequestInterceptor;
import feign.Retryer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;

public class GestoPagoFeignConfig {

    @Bean
    public Retryer retryer() {
        // PROHIBIDO el reintento automático HTTP en transacciones financieras sendTx
        return Retryer.NEVER_RETRY;
    }

    @Bean
    public RequestInterceptor gestoPagoBearerTokenInterceptor(
            GestoPagoTokenProvider tokenProvider,
            @Value("${gestopago.api.api-key:}") String apiKey) {
        return new BearerTokenInterceptor(tokenProvider, apiKey);
    }
}


