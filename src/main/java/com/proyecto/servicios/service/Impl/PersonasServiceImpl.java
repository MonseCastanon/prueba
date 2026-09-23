package com.proyecto.servicios.service.Impl;

import com.proyecto.servicios.entity.sf.Personas;
import com.proyecto.servicios.model.EliminaPersonaRequest;
import com.proyecto.servicios.model.GenericResponse;
import com.proyecto.servicios.model.PersonaResponse;
import com.proyecto.servicios.model.PersonasRequest;
import com.proyecto.servicios.repositorys.sf.PersonasRepository;
import com.proyecto.servicios.service.PersonaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class PersonasServiceImpl implements PersonaService {

    private static final Logger log = LoggerFactory.getLogger(PersonasServiceImpl.class);

    private final PersonasRepository personasRepository;

    public PersonasServiceImpl(PersonasRepository personasRepository) {
        this.personasRepository = personasRepository;
    }

    @Override
    @Transactional
    public PersonaResponse creaPersona(PersonasRequest personasRequest) {
        log.info("Creando nueva persona con nombre: {}", personasRequest.getNombre());
        Personas persona = new Personas();
        persona.setNombre(personasRequest.getNombre());
        persona.setApellidoP(personasRequest.getApellidoP());
        persona.setApellidoMaterno(personasRequest.getApellidoMaterno());
        personasRepository.save(persona);

        PersonaResponse response = new PersonaResponse();
        response.setCodigo(0);
        response.setMensaje("Persona creada exitosamente");
        response.setNombre(persona.getNombre());
        response.setApellidoP(persona.getApellidoP());
        response.setApellidoMaterno(persona.getApellidoMaterno());
        return response;
    }

    @Override
    @Transactional
    public GenericResponse actualizaPersona(PersonasRequest personasRequest) {
        log.info("Actualizando persona con nombre: {}", personasRequest.getNombre());
        GenericResponse response = new GenericResponse();
        Optional<Personas> existePersona = personasRepository.findByNombre(personasRequest.getNombre());

        if (existePersona.isPresent()) {
            Personas personaActualiza = existePersona.get();
            personaActualiza.setApellidoP(personasRequest.getApellidoP());
            personaActualiza.setApellidoMaterno(personasRequest.getApellidoMaterno());
            personasRepository.save(personaActualiza);
            response.setCodigo(0);
            response.setMensaje("La persona ha sido actualizada correctamente");
        } else {
            log.warn("No se encontró la persona con nombre: {}", personasRequest.getNombre());
            response.setCodigo(1);
            response.setMensaje("La persona no existe");
        }
        return response;
    }

    @Override
    @Transactional
    public GenericResponse eliminaPersona(EliminaPersonaRequest eliminaPersonaRequest) {
        log.info("Eliminando persona con nombre: {}", eliminaPersonaRequest.getNombre());
        GenericResponse response = new GenericResponse();
        Optional<Personas> existePersona = personasRepository.findByNombre(eliminaPersonaRequest.getNombre());

        if (existePersona.isPresent()) {
            personasRepository.delete(existePersona.get());
            response.setCodigo(0);
            response.setMensaje("La persona ha sido eliminada correctamente");
        } else {
            log.warn("No se encontró la persona a eliminar con nombre: {}", eliminaPersonaRequest.getNombre());
            response.setCodigo(1);
            response.setMensaje("La persona no existe");
        }
        return response;
    }
}
