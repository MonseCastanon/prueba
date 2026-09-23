package com.proyecto.servicios.enums;

import org.springframework.http.HttpStatus;

public enum CatProductoRespuestaEnum {

    EXITO(200, "OPERACION_EXITOSA", "Catálogo de productos obtenido correctamente", HttpStatus.OK),
    CATALOGO_NO_ENCONTRADO(404, "CATALOGO_NO_ENCONTRADO", "No existen productos registrados en el catálogo", HttpStatus.NOT_FOUND),
    ERROR_SINCRONIZACION_GP(502, "ERROR_SINCRONIZACION_GP", "No fue posible sincronizar el catálogo con el proveedor GP", HttpStatus.BAD_GATEWAY),
    ERROR_PARSING_XML(500, "ERROR_PARSING_XML", "Fallo al procesar y deserializar la estructura XML de productos", HttpStatus.INTERNAL_SERVER_ERROR),
    ERROR_BASE_DATOS_MONGO(500, "ERROR_MONGO_DB", "Ocurrió un error al consultar la base de datos de productos", HttpStatus.INTERNAL_SERVER_ERROR),
    TOKEN_NO_DISPONIBLE(401, "TOKEN_NO_DISPONIBLE", "No se encontró un token de autenticación válido para GP", HttpStatus.UNAUTHORIZED);

    private final int codigo;
    private final String clave;
    private final String mensaje;
    private final HttpStatus httpStatus;

    CatProductoRespuestaEnum(int codigo, String clave, String mensaje, HttpStatus httpStatus) {
        this.codigo = codigo;
        this.clave = clave;
        this.mensaje = mensaje;
        this.httpStatus = httpStatus;
    }

    public int getCodigo() {
        return codigo;
    }

    public String getClave() {
        return clave;
    }

    public String getMensaje() {
        return mensaje;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }
}
