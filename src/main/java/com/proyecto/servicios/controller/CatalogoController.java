package com.proyecto.servicios.controller;

import com.proyecto.servicios.model.*;
import com.proyecto.servicios.model.dto.EstadoDto;
import com.proyecto.servicios.model.dto.EstadoMunicipiosDto;
import com.proyecto.servicios.service.CatalogoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/catalogos")
@Tag(name = "Catálogos Geográficos", description = "Consulta estandarizada de países, estados de México, municipios y códigos postales sin PathVariables ni exposición de entidades JPA")
public class CatalogoController {

    private final CatalogoService catalogoService;

    public CatalogoController(CatalogoService catalogoService) {
        this.catalogoService = catalogoService;
    }

    @GetMapping(value = "/paises", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Consultar lista de países activos", description = "Retorna el catálogo de países mediante DTOs públicos.")
    public ResponseEntity<ApiResponse<List<PaisResponse>>> consultarPaises() {
        List<PaisResponse> paises = catalogoService.obtenerPaises().stream()
                .map(p -> PaisResponse.builder()
                        .id(p.getId())
                        .codigoIsoAlfa3(p.getCodigoIsoAlfa3())
                        .nombre(p.getNombre())
                        .activo(p.getActivo())
                        .build())
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.exito("Países consultados exitosamente", paises));
    }

    @GetMapping(value = "/estados", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            summary = "Consultar estados de la República Mexicana (API Externa)",
            description = "Consume una API externa para obtener los 32 estados de México con sus claves oficiales INEGI."
    )
    public ResponseEntity<ApiResponse<List<EstadoDto>>> consultarEstadosMexico() {
        return ResponseEntity.ok(ApiResponse.exito("Estados consultados exitosamente", catalogoService.obtenerEstadosMexico()));
    }

    @GetMapping(value = "/municipios", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            summary = "Consultar todos los municipios de México (API Externa)",
            description = "Retorna el catálogo completo de municipios de México agrupados por entidad federativa."
    )
    public ResponseEntity<ApiResponse<Map<String, List<String>>>> consultarTodosLosMunicipios() {
        return ResponseEntity.ok(ApiResponse.exito("Municipios consultados exitosamente", catalogoService.obtenerTodosLosMunicipios()));
    }

    @PostMapping(value = "/estados/municipios", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            summary = "Consultar municipios por estado de México vía RequestBody",
            description = "Obtiene los municipios de un estado recibiendo el filtro en el payload sin usar PathVariable."
    )
    public ResponseEntity<ApiResponse<EstadoMunicipiosDto>> consultarMunicipiosPorEstado(
            @Valid @RequestBody CatalogoMunicipioFiltroRequest request) {
        EstadoMunicipiosDto dto = catalogoService.obtenerMunicipiosPorEstado(request.getEstado());
        return ResponseEntity.ok(ApiResponse.exito("Municipios del estado consultados exitosamente", dto));
    }

    @PostMapping(value = "/paises/estados", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Consultar estados por código ISO de país vía RequestBody", description = "Retorna los estados asociados a un país recibiendo el código ISO en el body.")
    public ResponseEntity<ApiResponse<List<EstadoCatalogoResponse>>> consultarEstados(
            @Valid @RequestBody CatalogoPaisFiltroRequest request) {
        List<EstadoCatalogoResponse> estados = catalogoService.obtenerEstadosPorPais(request.getCodigoPaisIso()).stream()
                .map(e -> EstadoCatalogoResponse.builder()
                        .id(e.getId())
                        .claveEstado(e.getClaveEstado())
                        .nombre(e.getNombre())
                        .codigoPaisIso(e.getPais() != null ? e.getPais().getCodigoIsoAlfa3() : null)
                        .activo(e.getActivo())
                        .build())
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.exito("Estados del país consultados exitosamente", estados));
    }

    @PostMapping(value = "/codigo-postal", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Consultar municipios y colonias por Código Postal vía RequestBody")
    public ResponseEntity<ApiResponse<List<CodigoPostalResponse>>> consultarPorCodigoPostal(
            @Valid @RequestBody CodigoPostalFiltroRequest request) {
        List<CodigoPostalResponse> resultados = catalogoService.obtenerInformacionPorCodigoPostal(request.getCodigoPostal()).stream()
                .map(cp -> CodigoPostalResponse.builder()
                        .id(cp.getId())
                        .codigoPostal(cp.getCodigoPostal())
                        .municipioAlcaldia(cp.getMunicipioAlcaldia())
                        .colonia(cp.getColonia())
                        .estadoNombre(cp.getEstado() != null ? cp.getEstado().getNombre() : null)
                        .activo(cp.getActivo())
                        .build())
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.exito("Código postal consultado exitosamente", resultados));
    }
}
