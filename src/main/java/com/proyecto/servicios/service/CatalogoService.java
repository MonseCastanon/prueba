package com.proyecto.servicios.service;

import com.proyecto.servicios.entity.sf.CodigoPostalCatalogo;
import com.proyecto.servicios.entity.sf.EstadoCatalogo;
import com.proyecto.servicios.entity.sf.Pais;
import com.proyecto.servicios.model.dto.EstadoDto;
import com.proyecto.servicios.model.dto.EstadoMunicipiosDto;

import java.util.List;
import java.util.Map;

public interface CatalogoService {

    /**
     * Catálogo local de países registrados y activos.
     */
    List<Pais> obtenerPaises();

    /**
     * Catálogo de estados de la República Mexicana consumido desde API externa.
     */
    List<EstadoDto> obtenerEstadosMexico();

    /**
     * Catálogo de todos los municipios de México agrupados por estado consumido desde API externa.
     */
    Map<String, List<String>> obtenerTodosLosMunicipios();

    /**
     * Catálogo de municipios filtrados por estado (nombre, abreviatura o clave INEGI) desde API externa.
     */
    EstadoMunicipiosDto obtenerMunicipiosPorEstado(String estado);

    /**
     * Catálogo de estados por código ISO de país desde base de datos.
     */
    List<EstadoCatalogo> obtenerEstadosPorPais(String codigoPaisIso);

    /**
     * Catálogo de códigos postales desde base de datos.
     */
    List<CodigoPostalCatalogo> obtenerInformacionPorCodigoPostal(String codigoPostal);
}
