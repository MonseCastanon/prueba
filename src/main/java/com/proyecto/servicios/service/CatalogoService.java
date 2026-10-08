package com.proyecto.servicios.service;

import com.proyecto.servicios.entity.sf.CodigoPostalCatalogo;
import com.proyecto.servicios.entity.sf.EstadoCatalogo;
import com.proyecto.servicios.entity.sf.Pais;

import java.util.List;

public interface CatalogoService {
    List<Pais> obtenerPaises();
    List<EstadoCatalogo> obtenerEstadosPorPais(String codigoPaisIso);
    List<CodigoPostalCatalogo> obtenerInformacionPorCodigoPostal(String codigoPostal);
}
