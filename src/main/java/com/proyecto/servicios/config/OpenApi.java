package com.proyecto.servicios.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApi {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("API Catálogo de Productos y Servicios GP")
                        .version("1.0.0")
                        .description("Servicio de sincronización y consulta de productos con PuntoRed/GestoPago y MongoDB")
                        .contact(new Contact()
                                .name("Soporte Hasani")
                                .email("soporte@hasani.com")));
    }
}
