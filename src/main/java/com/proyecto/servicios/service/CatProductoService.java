package com.proyecto.servicios.service;

import com.proyecto.servicios.model.gestopago.CatProductoResponse;

public interface CatProductoService {

    /**
     * Consulta el catálogo de productos almacenado en MongoDB.
     * Si encuentra registros retorna 200 con el JSON de productos.
     * Si no encuentra o falla, retorna la respuesta tipificada con Enums.
     */
    CatProductoResponse obtenerCatalogo();

    /**
     * Ejecuta la sincronización descargando el XML de GP, parseando con JAXB y guardando en MongoDB.
     */
    void sincronizarCatalogoDesdeGp();
}
