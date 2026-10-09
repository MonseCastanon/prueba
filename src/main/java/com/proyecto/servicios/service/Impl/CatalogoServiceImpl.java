package com.proyecto.servicios.service.Impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.proyecto.servicios.client.MexicoGeografiaClient;
import com.proyecto.servicios.entity.sf.CodigoPostalCatalogo;
import com.proyecto.servicios.entity.sf.EstadoCatalogo;
import com.proyecto.servicios.entity.sf.Pais;
import com.proyecto.servicios.exception.ResourceNotFoundException;
import com.proyecto.servicios.model.dto.EstadoDto;
import com.proyecto.servicios.model.dto.EstadoMunicipiosDto;
import com.proyecto.servicios.repositorys.sf.CodigoPostalRepository;
import com.proyecto.servicios.repositorys.sf.EstadoRepository;
import com.proyecto.servicios.repositorys.sf.PaisRepository;
import com.proyecto.servicios.service.CatalogoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.Normalizer;
import java.util.List;
import java.util.Map;

@Service
public class CatalogoServiceImpl implements CatalogoService {

    private static final Logger log = LoggerFactory.getLogger(CatalogoServiceImpl.class);

    private final PaisRepository paisRepository;
    private final EstadoRepository estadoRepository;
    private final CodigoPostalRepository codigoPostalRepository;
    private final MexicoGeografiaClient mexicoGeografiaClient;
    private final ObjectMapper objectMapper;

    public CatalogoServiceImpl(PaisRepository paisRepository,
            EstadoRepository estadoRepository,
            CodigoPostalRepository codigoPostalRepository,
            MexicoGeografiaClient mexicoGeografiaClient,
            ObjectMapper objectMapper) {
        this.paisRepository = paisRepository;
        this.estadoRepository = estadoRepository;
        this.codigoPostalRepository = codigoPostalRepository;
        this.mexicoGeografiaClient = mexicoGeografiaClient;
        this.objectMapper = objectMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Pais> obtenerPaises() {
        return paisRepository.findByActivoTrue();
    }

    @Override
    @Cacheable(value = "catalogoEstadosMexico")
    public List<EstadoDto> obtenerEstadosMexico() {
        log.info("Consultando catálogo de estados de México desde API externa...");
        try {
            String rawJson = mexicoGeografiaClient.obtenerEstadosRaw();
            if (rawJson == null || rawJson.trim().isEmpty()) {
                throw new ResourceNotFoundException("No se recibieron datos de estados desde el servicio externo");
            }
            List<EstadoDto> estados = objectMapper.readValue(rawJson, new TypeReference<List<EstadoDto>>() {
            });
            if (estados == null || estados.isEmpty()) {
                throw new ResourceNotFoundException("No se pudieron procesar los estados desde el servicio externo");
            }
            return estados;
        } catch (Exception e) {
            log.error("Error al consumir API externa de estados de México: {}", e.getMessage());
            throw new ResourceNotFoundException("Error al consultar el catálogo externo de estados: " + e.getMessage());
        }
    }

    @Override
    @Cacheable(value = "catalogoMunicipiosMexico")
    public Map<String, List<String>> obtenerTodosLosMunicipios() {
        log.info("Consultando catálogo completo de municipios desde API externa...");
        try {
            String rawJson = mexicoGeografiaClient.obtenerEstadosConMunicipiosRaw();
            if (rawJson == null || rawJson.trim().isEmpty()) {
                throw new ResourceNotFoundException("No se recibieron datos de municipios desde el servicio externo");
            }
            Map<String, List<String>> estadosMunicipios = objectMapper.readValue(rawJson, new TypeReference<Map<String, List<String>>>() {
            });
            if (estadosMunicipios == null || estadosMunicipios.isEmpty()) {
                throw new ResourceNotFoundException("No se pudieron procesar los municipios desde el servicio externo");
            }
            return estadosMunicipios;
        } catch (Exception e) {
            log.error("Error al consumir API externa de municipios de México: {}", e.getMessage());
            throw new ResourceNotFoundException("Error al consultar el catálogo externo de municipios: " + e.getMessage());
        }
    }

    @Override
    public EstadoMunicipiosDto obtenerMunicipiosPorEstado(String estadoParam) {
        if (estadoParam == null || estadoParam.trim().isEmpty()) {
            throw new ResourceNotFoundException("El parámetro de estado no puede estar vacío");
        }

        String normalizedQuery = normalizar(estadoParam);
        Map<String, List<String>> todosLosMunicipios = obtenerTodosLosMunicipios();

        // 1. Búsqueda directa por coincidencia de nombre exacto o normalizado
        for (Map.Entry<String, List<String>> entry : todosLosMunicipios.entrySet()) {
            String stateKey = entry.getKey();
            if (normalizar(stateKey).equals(normalizedQuery)) {
                return EstadoMunicipiosDto.builder()
                        .estado(stateKey)
                        .totalMunicipios(entry.getValue().size())
                        .municipios(entry.getValue())
                        .build();
            }
        }

        // 2. Búsqueda por clave de entidad (ej. "09", "14") o abreviatura (ej. "CDMX", "JAL")
        List<EstadoDto> estadosInfo = obtenerEstadosMexico();
        for (EstadoDto estadoDto : estadosInfo) {
            boolean matchesClaveEntidad = estadoDto.getClaveEntidad() != null && normalizar(estadoDto.getClaveEntidad()).equals(normalizedQuery);
            boolean matchesClave = estadoDto.getClave() != null && normalizar(estadoDto.getClave()).replaceAll("\\.", "").equals(normalizedQuery.replaceAll("\\.", ""));
            boolean matchesNombre = estadoDto.getNombre() != null && normalizar(estadoDto.getNombre()).equals(normalizedQuery);

            if (matchesClaveEntidad || matchesClave || matchesNombre) {
                String targetStateNormalized = normalizar(estadoDto.getNombre());
                for (Map.Entry<String, List<String>> entry : todosLosMunicipios.entrySet()) {
                    String stateKeyNormalized = normalizar(entry.getKey());
                    if (stateKeyNormalized.equals(targetStateNormalized)
                            || stateKeyNormalized.startsWith(targetStateNormalized)
                            || targetStateNormalized.startsWith(stateKeyNormalized)) {
                        return EstadoMunicipiosDto.builder()
                                .estado(entry.getKey())
                                .totalMunicipios(entry.getValue().size())
                                .municipios(entry.getValue())
                                .build();
                    }
                }
            }
        }

        // 3. Búsqueda flexible por contención de texto (ej. "Michoacan" en "Michoacán de Ocampo")
        for (Map.Entry<String, List<String>> entry : todosLosMunicipios.entrySet()) {
            String stateKeyNormalized = normalizar(entry.getKey());
            if (stateKeyNormalized.contains(normalizedQuery) || normalizedQuery.contains(stateKeyNormalized)) {
                return EstadoMunicipiosDto.builder()
                        .estado(entry.getKey())
                        .totalMunicipios(entry.getValue().size())
                        .municipios(entry.getValue())
                        .build();
            }
        }

        throw new ResourceNotFoundException("No se encontraron municipios para el estado: " + estadoParam);
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

    private String normalizar(String texto) {
        if (texto == null) {
            return "";
        }
        String normalized = Normalizer.normalize(texto, Normalizer.Form.NFD);
        return normalized.replaceAll("\\p{M}", "").toLowerCase().trim();
    }
}
