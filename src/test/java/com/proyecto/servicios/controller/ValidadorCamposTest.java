package com.proyecto.servicios.controller;

import com.proyecto.servicios.model.GenericResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ValidadorCamposTest {

    private final ValidadorCampos validadorCampos = new ValidadorCampos();

    @Test
    @DisplayName("dtoValidation con Errors procesa los FieldErrors y devuelve 400 con GenericResponse")
    void testDtoValidationConErrors() {
        Object target = new Object();
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(target, "requestDto");
        bindingResult.addError(new FieldError("requestDto", "correo", "El formato del correo es inválido"));
        bindingResult.addError(new FieldError("requestDto", "password", "La contraseña es requerida"));

        ResponseEntity<GenericResponse> response = validadorCampos.dtoValidation(bindingResult);

        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().getCodigo());
        assertTrue(response.getBody().getMensaje().contains("Datos no validos"));
        assertTrue(response.getBody().getMensaje().contains("correo"));
        assertTrue(response.getBody().getMensaje().contains("password"));
    }

    @Test
    @DisplayName("dtoValidation con MethodArgumentNotValidException extrae el BindingResult correctamente")
    void testDtoValidationConException() {
        Object target = new Object();
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(target, "requestDto");
        bindingResult.addError(new FieldError("requestDto", "curp", "La CURP es obligatoria"));

        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        when(ex.getBindingResult()).thenReturn(bindingResult);

        ResponseEntity<GenericResponse> response = validadorCampos.dtoValidation(ex);

        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().getCodigo());
        assertTrue(response.getBody().getMensaje().contains("curp"));
    }
}
