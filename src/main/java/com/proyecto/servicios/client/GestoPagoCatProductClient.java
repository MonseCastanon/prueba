package com.proyecto.servicios.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(
        name = "gestoPagoCatProductClient",
        url = "${gestopago.api.base-url:${gestopago.service.url:https://gestopago.portalventas.net}}"
)
public interface GestoPagoCatProductClient {

    /**
     * Consume el endpoint getProductList de GP que retorna el XML del catálogo.
     * Se puede enviar el Bearer Token y opcionalmente el X-API-Key.
     */
    @GetMapping(
            value = "${gestopago.endpoints.get-product-list:${gestopago.service.endpoints.cat-product:/sistema/service/getProductList.do}}",
            produces = {MediaType.APPLICATION_XML_VALUE, MediaType.TEXT_XML_VALUE}
    )
    String getProductListXml(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String bearerToken,
            @RequestHeader(value = "X-API-Key", required = false) String apiKey
    );
}

