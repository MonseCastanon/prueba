package com.proyecto.servicios.service.Impl;

import com.proyecto.servicios.entity.sf.CodigoPostalCatalogo;
import com.proyecto.servicios.entity.sf.EstadoCatalogo;
import com.proyecto.servicios.entity.sf.Pais;
import com.proyecto.servicios.exception.ResourceNotFoundException;
import com.proyecto.servicios.repositorys.sf.CodigoPostalRepository;
import com.proyecto.servicios.repositorys.sf.EstadoRepository;
import com.proyecto.servicios.repositorys.sf.PaisRepository;
import com.proyecto.servicios.service.CatalogoService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CatalogoServiceImpl implements CatalogoService {

    private final PaisRepository paisRepository;
    private final EstadoRepository estadoRepository;
    private final CodigoPostalRepository codigoPostalRepository;

    public CatalogoServiceImpl(PaisRepository paisRepository,
                               EstadoRepository estadoRepository,
                               CodigoPostalRepository codigoPostalRepository) {
        this.paisRepository = paisRepository;
        this.estadoRepository = estadoRepository;
        this.codigoPostalRepository = codigoPostalRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Pais> obtenerPaises() {
        return paisRepository.findByActivoTrue();
    }

    @Override
    @Transactional(readOnly = true)
    public List<EstadoCatalogo> obtenerEstadosPorPais(String codigoPaisIso) {
        return estadoRepository.findByPaisCodigoIsoAlfa3AndActivoTrue(codigoPaisIso.trim().toUpperCase());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CodigoPostalCatalogo> obtenerInformacionPorCodigoPostal(String codigoPostal) {
        List<CodigoPostalCatalogo> list = codigoPostalRepository.findByCodigoPostalAndActivoTrue(codigoPostal.trim());
        if (list.isEmpty()) {
            throw new ResourceNotFoundException("No se encontró información para el código postal: " + codigoPostal);
        }
        return list;
    }
}
