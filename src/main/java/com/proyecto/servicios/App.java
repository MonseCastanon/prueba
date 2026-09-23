package com.proyecto.servicios;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.info.BuildProperties;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@SpringBootApplication
@EnableScheduling
@EnableFeignClients
public class App implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(App.class);

    private final ApplicationContext context;

    public App(ApplicationContext context) {
        this.context = context;
    }

    public static void main(String[] args) {
        SpringApplication.run(App.class, args);
    }

    @Override
    public void run(String... args) {
        try {
            BuildProperties buildProperties = context.getBean(BuildProperties.class);
            displayInfo(buildProperties);
        } catch (Exception e) {
            log.info("Servicio iniciado correctamente sin BuildProperties.");
        }
    }

    private static void displayInfo(BuildProperties buildProperties) {
        if (buildProperties != null && buildProperties.getTime() != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.systemDefault());
            String out = formatter.format(buildProperties.getTime());
            log.info("Nombre artefacto: {}\nVersión: {}\nFecha Compilación: {}\nArtefacto: {}\nGrupo: {}",
                    buildProperties.getName(),
                    buildProperties.getVersion(),
                    out,
                    buildProperties.getArtifact(),
                    buildProperties.getGroup());
        }
    }
}
