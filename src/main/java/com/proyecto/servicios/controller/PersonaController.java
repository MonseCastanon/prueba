package com.proyecto.servicios.controller;

import com.proyecto.servicios.model.EliminaPersonaRequest;
import com.proyecto.servicios.model.GenericResponse;
import com.proyecto.servicios.model.PersonaResponse;
import com.proyecto.servicios.model.PersonasRequest;
import com.proyecto.servicios.service.PersonaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "Personas", description = "Operaciones CRUD para la gestión de personas")
public class PersonaController {

    private final PersonaService personaService;

    public PersonaController(PersonaService personaService) {
        this.personaService = personaService;
    }

    @PostMapping(
            value = "/personas",
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    @Operation(summary = "Crea un nuevo registro de persona")
    public ResponseEntity<PersonaResponse> crearPersona(@Valid @RequestBody PersonasRequest personasRequest) {
        return ResponseEntity.ok(personaService.creaPersona(personasRequest));
    }

    @PutMapping(
            value = "/personasActualiza",
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    @Operation(summary = "Actualiza los datos de una persona existente")
    public ResponseEntity<GenericResponse> actualizarPersona(@Valid @RequestBody PersonasRequest personasRequest) {
        return ResponseEntity.ok(personaService.actualizaPersona(personasRequest));
    }

    @PutMapping(
            value = "/personasElimina",
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    @Operation(summary = "Elimina el registro de una persona")
    public ResponseEntity<GenericResponse> eliminarPersona(@Valid @RequestBody EliminaPersonaRequest personasRequest) {
        return ResponseEntity.ok(personaService.eliminaPersona(personasRequest));
    }
}
