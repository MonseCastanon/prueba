package com.proyecto.servicios.controller;

import com.proyecto.servicios.entity.sf.CodigoPostalCatalogo;
import com.proyecto.servicios.entity.sf.EstadoCatalogo;
import com.proyecto.servicios.entity.sf.Pais;
import com.proyecto.servicios.service.CatalogoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/catalogos")
@Tag(name = "Catálogos Geográficos", description = "Consulta de países, estados y códigos postales")
public class CatalogoController {

    private final CatalogoService catalogoService;

    public CatalogoController(CatalogoService catalogoService) {
        this.catalogoService = catalogoService;
    }

    @GetMapping(value = "/paises", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Consultar lista de países activos")
    public ResponseEntity<List<Pais>> consultarPaises() {
        return ResponseEntity.ok(catalogoService.obtenerPaises());
    }

    @GetMapping(value = "/paises/{codigoPaisIso}/estados", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Consultar estados por código ISO de país (ej. MEX)")
    public ResponseEntity<List<EstadoCatalogo>> consultarEstados(@PathVariable String codigoPaisIso) {
        return ResponseEntity.ok(catalogoService.obtenerEstadosPorPais(codigoPaisIso));
    }

    @GetMapping(value = "/codigo-postal/{codigoPostal}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Consultar municipios y colonias por Código Postal")
    public ResponseEntity<List<CodigoPostalCatalogo>> consultarPorCodigoPostal(@PathVariable String codigoPostal) {
        return ResponseEntity.ok(catalogoService.obtenerInformacionPorCodigoPostal(codigoPostal));
    }
}
