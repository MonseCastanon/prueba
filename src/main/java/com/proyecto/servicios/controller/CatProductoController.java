package com.proyecto.servicios.controller;

import com.proyecto.servicios.enums.CatProductoRespuestaEnum;
import com.proyecto.servicios.model.gestopago.CatProductoResponse;
import com.proyecto.servicios.service.CatProductoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/productos")
@Tag(name = "Catálogo de Productos", description = "Endpoints para la consulta y sincronización de productos")
@Slf4j
public class CatProductoController {

    private final CatProductoService catProductoService;

    public CatProductoController(CatProductoService catProductoService) {
        this.catProductoService = catProductoService;
    }

    @GetMapping("/catalogo")
    @Operation(summary = "Obtiene el catálogo de productos en formato JSON desde MongoDB")
    public ResponseEntity<CatProductoResponse> obtenerCatalogo() {
        CatProductoResponse response = catProductoService.obtenerCatalogo();

        if (response.getCodigo() != null && response.getCodigo() == CatProductoRespuestaEnum.EXITO.getCodigo()) {
            return ResponseEntity.ok(response);
        } else if (response.getCodigo() != null && response.getCodigo() == CatProductoRespuestaEnum.CATALOGO_NO_ENCONTRADO.getCodigo()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PostMapping("/sincronizar")
    @Operation(summary = "Fuerza la ejecución manual de la sincronización con GP catProduct")
    public ResponseEntity<String> forzarSincronizacion() {
        catProductoService.sincronizarCatalogoDesdeGp();
        return ResponseEntity.ok("Sincronización manual ejecutada.");
    }
}
