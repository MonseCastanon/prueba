package com.proyecto.servicios.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(
        name = "mexicoGeografiaClient",
        url = "${mexico.geografia.api.base-url:https://raw.githubusercontent.com/cisnerosnow/json-estados-municipios-mexico/master}"
)
public interface MexicoGeografiaClient {

    @GetMapping(value = "/estados.json")
    String obtenerEstadosRaw();

    @GetMapping(value = "/estados-municipios.json")
    String obtenerEstadosConMunicipiosRaw();
}
