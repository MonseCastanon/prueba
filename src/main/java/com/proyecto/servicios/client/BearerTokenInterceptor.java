package com.proyecto.servicios.client;

import com.proyecto.servicios.service.GestoPagoTokenProvider;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.http.HttpHeaders;

public class BearerTokenInterceptor implements RequestInterceptor {

    private final GestoPagoTokenProvider tokenProvider;
    private final String apiKey;

    public BearerTokenInterceptor(GestoPagoTokenProvider tokenProvider, String apiKey) {
        this.tokenProvider = tokenProvider;
        this.apiKey = apiKey;
    }

    @Override
    public void apply(RequestTemplate template) {
        if (template.url() != null && template.url().contains("/authenticate")) {
            return;
        }
        String token = tokenProvider.tokenVigente();
        if (token != null && !token.isBlank()) {
            template.header(HttpHeaders.AUTHORIZATION, "Bearer " + token);
        }
        if (apiKey != null && !apiKey.isBlank()) {
            template.header("X-API-Key", apiKey);
        }
    }
}


