package com.proyecto.servicios.controller;

import com.proyecto.servicios.model.GenericResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.Errors;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.HashMap;
import java.util.Map;

/**
 * Controlador de asesoramiento global para validación declarativa de DTOs.
 * Captura MethodArgumentNotValidException y retorna una respuesta simplificada GenericResponse.
 */
@Slf4j
@ControllerAdvice
@Order(Ordered.LOWEST_PRECEDENCE)
public class ValidadorCampos {

    /**
     * En caso de que alguna validación no se cumpla para algún DTO se lanza MethodArgumentNotValidException.
     * Este método la captura y obtiene el BindingResult para procesar los errores.
     *
     * @param ex excepción capturada de validación de argumentos en el controlador
     * @return respuesta HTTP 400 Bad Request con GenericResponse
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<GenericResponse> dtoValidation(MethodArgumentNotValidException ex) {
        if (log.isWarnEnabled()) {
            log.warn("ValidadorCampos capturó fallo de validación en DTO: {}",
                    ex.getBindingResult() != null ? ex.getBindingResult().getObjectName() : "objeto");
        }
        return dtoValidation((Errors) ex.getBindingResult());
    }

    /**
     * Procesa los errores de validación de cada campo y construye el GenericResponse con los mensajes descriptivos.
     *
     * @param errors interfaz Errors que contiene los FieldErrors del DTO
     * @return ResponseEntity con GenericResponse y código HTTP BAD_REQUEST
     */
    public ResponseEntity<GenericResponse> dtoValidation(Errors errors) {
        GenericResponse genericResponse = new GenericResponse();
        Map<String, String> listError = new HashMap<>();

        for (FieldError fieldError : errors.getFieldErrors()) {
            listError.put(fieldError.getField(), fieldError.getDefaultMessage());
        }

        genericResponse.setCodigo(1);
        genericResponse.setMensaje("Datos no validos: " + listError);
        return new ResponseEntity<>(genericResponse, HttpStatus.BAD_REQUEST);
    }
}
