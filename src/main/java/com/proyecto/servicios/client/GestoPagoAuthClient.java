package com.proyecto.servicios.client;

import com.proyecto.servicios.model.gestopago.GestoPagoAuthResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "gestoPagoAuth", url = "${gestopago.api.base-url}")
public interface GestoPagoAuthClient {

    @PostMapping(
            value = "${gestopago.endpoints.auth:/sistema/app/jwt-gp/authenticate/}",
            consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE
    )
    GestoPagoAuthResponse authenticate(@RequestBody MultiValueMap<String, String> formParams);
}
