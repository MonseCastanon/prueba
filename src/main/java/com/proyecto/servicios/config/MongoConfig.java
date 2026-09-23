package com.proyecto.servicios.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

/**
 * Configuración dedicada para habilitar repositorios de MongoDB
 * delimitando el escaneo al paquete específico de MongoDB.
 */
@Configuration
@EnableMongoRepositories(basePackages = "com.proyecto.servicios.repositorys.mongo")
public class MongoConfig {
}