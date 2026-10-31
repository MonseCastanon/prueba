package com.proyecto.servicios.exception;

import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNotFound(ResourceNotFoundException ex) {
        log.warn("Recurso no encontrado: {}", ex.getMessage());
        return buildResponse(HttpStatus.NOT_FOUND, ex.getMessage(), null);
    }

    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<Map<String, Object>> handleDuplicate(DuplicateResourceException ex) {
        log.warn("Conflicto por duplicidad: {}", ex.getMessage());
        return buildResponse(HttpStatus.CONFLICT, ex.getMessage(), null);
    }

    @ExceptionHandler(DuplicateRequestException.class)
    public ResponseEntity<Map<String, Object>> handleDuplicateRequest(DuplicateRequestException ex) {
        log.warn("Conflicto por IdDocumento duplicado: {}", ex.getMessage());
        return buildResponse(HttpStatus.CONFLICT, ex.getMessage(), null);
    }

    @ExceptionHandler(BusinessRuleException.class)
    public ResponseEntity<Map<String, Object>> handleBusinessRule(BusinessRuleException ex) {
        log.warn("Violación de regla de negocio: {}", ex.getMessage());
        return buildResponse(HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage(), null);
    }

    @ExceptionHandler(AccountBlockedException.class)
    public ResponseEntity<Map<String, Object>> handleAccountBlocked(AccountBlockedException ex) {
        log.warn("Operación rechazada por cuenta bloqueada: {}", ex.getMessage());
        return buildResponse(HttpStatus.FORBIDDEN, ex.getMessage(), null);
    }

    @ExceptionHandler(PinBlockedException.class)
    public ResponseEntity<Map<String, Object>> handlePinBlocked(PinBlockedException ex) {
        log.warn("Operación rechazada por PIN bloqueado: {}", ex.getMessage());
        return buildResponse(HttpStatus.LOCKED, ex.getMessage(), null);
    }

    @ExceptionHandler(TooManyRequestsException.class)
    public ResponseEntity<Map<String, Object>> handleTooManyRequests(TooManyRequestsException ex) {
        log.warn("Rate limit excedido: {}", ex.getMessage());
        return buildResponse(HttpStatus.TOO_MANY_REQUESTS, ex.getMessage(), null);
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidCredentials(InvalidCredentialsException ex) {
        log.warn("Fallo de autenticación o PIN inválido");
        return buildResponse(HttpStatus.UNAUTHORIZED, "Credenciales o PIN inválidos", null);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, Object>> handleAccessDenied(AccessDeniedException ex) {
        log.warn("Acceso denegado: {}", ex.getMessage());
        return buildResponse(HttpStatus.FORBIDDEN, "No posee los privilegios requeridos para esta operación", null);
    }

    @ExceptionHandler(PessimisticLockingFailureException.class)
    public ResponseEntity<Map<String, Object>> handlePessimisticLocking(PessimisticLockingFailureException ex) {
        log.warn("Bloqueo de concurrencia en cuenta bancaria: {}", ex.getMessage());
        return buildResponse(HttpStatus.CONFLICT, "Operación en curso sobre la cuenta bancaria. Por favor reintente.", null);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, Object>> handleDataIntegrity(DataIntegrityViolationException ex) {
        log.warn("Violación de integridad de datos en BD: {}", ex.getMostSpecificCause().getMessage());
        return buildResponse(HttpStatus.CONFLICT, "Conflicto con una operación registrada previamente.", null);
    }

    @ExceptionHandler(ExternalServiceException.class)
    public ResponseEntity<Map<String, Object>> handleExternalService(ExternalServiceException ex) {
        log.error("Fallo con proveedor externo: status={}, tipo={}", ex.getStatusCode(), ex.getClass().getSimpleName());
        return buildResponse(HttpStatus.SERVICE_UNAVAILABLE, "Servicio temporalmente no disponible por inconvenientes con el switch de servicios", null);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationErrors(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            errors.put(error.getField(), error.getDefaultMessage());
        }
        log.warn("Errores de validación en la solicitud: {}", errors);
        return buildResponse(HttpStatus.BAD_REQUEST, "Los datos enviados no cumplen con las validaciones requeridas", errors);
    }

    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<Map<String, Object>> handleHandlerMethodValidation(HandlerMethodValidationException ex) {
        log.warn("Error de validación en parámetros del controlador: {}", ex.getMessage());
        return buildResponse(HttpStatus.BAD_REQUEST, "Los datos enviados no cumplen con las validaciones requeridas", null);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Map<String, Object>> handleConstraintViolation(ConstraintViolationException ex) {
        log.warn("Violación de restricciones de validación: {}", ex.getMessage());
        return buildResponse(HttpStatus.BAD_REQUEST, "Los parámetros de la solicitud son inválidos", null);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> handleNotReadable(HttpMessageNotReadableException ex) {
        log.warn("JSON mal formado o campos desconocidos en la solicitud: {}", ex.getMessage());
        return buildResponse(HttpStatus.BAD_REQUEST, "El cuerpo de la solicitud es inválido o contiene parámetros no permitidos", null);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgument(IllegalArgumentException ex) {
        log.warn("Argumento inválido: {}", ex.getMessage());
        return buildResponse(HttpStatus.BAD_REQUEST, "Solicitud inválida o argumento no admitido", null);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<Map<String, Object>> handleMethodNotSupported(HttpRequestMethodNotSupportedException ex) {
        log.warn("Método HTTP no soportado: {}", ex.getMethod());
        return buildResponse(HttpStatus.METHOD_NOT_ALLOWED, "El método HTTP " + ex.getMethod() + " no está soportado en esta ruta", null);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(Exception ex) {
        String correlationId = UUID.randomUUID().toString();
        log.error("Error no controlado en la aplicación [correlationId={}]: ", correlationId, ex);
        Map<String, Object> detalles = Map.of("correlationId", correlationId);
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Ha ocurrido un error interno. Por favor intente más tarde", detalles);
    }

    private ResponseEntity<Map<String, Object>> buildResponse(HttpStatus status, String mensaje, Object detalles) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", OffsetDateTime.now());
        body.put("status", status.value());
        body.put("error", status.getReasonPhrase());
        body.put("mensaje", mensaje);
        if (detalles != null) {
            body.put("detalles", detalles);
        }
        return new ResponseEntity<>(body, status);
    }
}
