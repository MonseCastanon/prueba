package com.proyecto.servicios.controller;

import com.proyecto.servicios.entity.sf.CodigoPostalCatalogo;
import com.proyecto.servicios.entity.sf.EstadoCatalogo;
import com.proyecto.servicios.entity.sf.Pais;
import com.proyecto.servicios.model.dto.EstadoDto;
import com.proyecto.servicios.model.dto.EstadoMunicipiosDto;
import com.proyecto.servicios.service.CatalogoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/catalogos")
@Tag(name = "Catálogos Geográficos", description = "Consulta de países, estados de México, municipios y códigos postales")
public class CatalogoController {

    private final CatalogoService catalogoService;

    public CatalogoController(CatalogoService catalogoService) {
        this.catalogoService = catalogoService;
    }

    @GetMapping(value = "/paises", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Consultar lista de países activos", description = "Retorna el catálogo de países registrados en la base de datos.")
    public ResponseEntity<List<Pais>> consultarPaises() {
        return ResponseEntity.ok(catalogoService.obtenerPaises());
    }

    @GetMapping(value = "/estados", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            summary = "Consultar estados de la República Mexicana (API Externa)",
            description = "Consume una API externa para obtener los 32 estados de México con sus claves oficiales INEGI."
    )
    public ResponseEntity<List<EstadoDto>> consultarEstadosMexico() {
        return ResponseEntity.ok(catalogoService.obtenerEstadosMexico());
    }

    @GetMapping(value = "/municipios", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            summary = "Consultar todos los municipios de México (API Externa)",
            description = "Retorna el catálogo completo de municipios de México agrupados por entidad federativa."
    )
    public ResponseEntity<Map<String, List<String>>> consultarTodosLosMunicipios() {
        return ResponseEntity.ok(catalogoService.obtenerTodosLosMunicipios());
    }

    @GetMapping(value = "/estados/{estado}/municipios", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            summary = "Consultar municipios por estado de México (API Externa)",
            description = "Obtiene los municipios de un estado específico. Permite buscar por nombre (ej. 'Jalisco'), abreviatura (ej. 'CDMX') o clave INEGI (ej. '14')."
    )
    public ResponseEntity<EstadoMunicipiosDto> consultarMunicipiosPorEstado(
            @Parameter(description = "Nombre, clave INEGI o abreviatura del estado", example = "Jalisco")
            @PathVariable String estado) {
        return ResponseEntity.ok(catalogoService.obtenerMunicipiosPorEstado(estado));
    }

    @GetMapping(value = "/paises/{codigoPaisIso}/estados", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Consultar estados por código ISO de país (ej. MEX)", description = "Retorna los estados asociados a un país desde la base de datos.")
    public ResponseEntity<List<EstadoCatalogo>> consultarEstados(@PathVariable String codigoPaisIso) {
        return ResponseEntity.ok(catalogoService.obtenerEstadosPorPais(codigoPaisIso));
    }

    @GetMapping(value = "/codigo-postal/{codigoPostal}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Consultar municipios y colonias por Código Postal")
    public ResponseEntity<List<CodigoPostalCatalogo>> consultarPorCodigoPostal(@PathVariable String codigoPostal) {
        return ResponseEntity.ok(catalogoService.obtenerInformacionPorCodigoPostal(codigoPostal));
    }
}
