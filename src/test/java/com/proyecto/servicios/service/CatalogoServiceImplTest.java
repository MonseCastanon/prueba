package com.proyecto.servicios.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.proyecto.servicios.client.MexicoGeografiaClient;
import com.proyecto.servicios.entity.sf.Pais;
import com.proyecto.servicios.exception.ResourceNotFoundException;
import com.proyecto.servicios.model.dto.EstadoDto;
import com.proyecto.servicios.model.dto.EstadoMunicipiosDto;
import com.proyecto.servicios.repositorys.sf.CodigoPostalRepository;
import com.proyecto.servicios.repositorys.sf.EstadoRepository;
import com.proyecto.servicios.repositorys.sf.PaisRepository;
import com.proyecto.servicios.service.Impl.CatalogoServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CatalogoServiceImplTest {

    @Mock
    private PaisRepository paisRepository;

    @Mock
    private EstadoRepository estadoRepository;

    @Mock
    private CodigoPostalRepository codigoPostalRepository;

    @Mock
    private MexicoGeografiaClient mexicoGeografiaClient;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    private CatalogoServiceImpl catalogoService;

    private String estadosJsonMock;
    private String municipiosJsonMock;

    @BeforeEach
    void setUp() {
        estadosJsonMock = """
                [
                    {"cve_ent": "09", "clave": "CDMX", "nombre": "CIUDAD DE MÉXICO"},
                    {"cve_ent": "14", "clave": "Jal.", "nombre": "JALISCO"},
                    {"cve_ent": "19", "clave": "NL", "nombre": "NUEVO LEÓN"}
                ]
                """;

        municipiosJsonMock = """
                {
                    "Ciudad de México": ["Cuauhtémoc", "Benito Juárez", "Coyoacán"],
                    "Jalisco": ["Guadalajara", "Zapopan", "Tlaquepaque"],
                    "Nuevo León": ["Monterrey", "San Pedro Garza García", "Guadalupe"]
                }
                """;
    }

    @Test
    @DisplayName("Debe conservar el catálogo de países desde base de datos")
    void testObtenerPaises() {
        List<Pais> paises = List.of(new Pais(1L, "MEX", "MEXICO", true));
        when(paisRepository.findByActivoTrue()).thenReturn(paises);

        List<Pais> resultado = catalogoService.obtenerPaises();

        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertEquals("MEX", resultado.get(0).getCodigoIsoAlfa3());
        verify(paisRepository, times(1)).findByActivoTrue();
    }

    @Test
    @DisplayName("Debe obtener estados de México consumiendo el cliente Feign de la API externa")
    void testObtenerEstadosMexico() {
        when(mexicoGeografiaClient.obtenerEstadosRaw()).thenReturn(estadosJsonMock);

        List<EstadoDto> resultado = catalogoService.obtenerEstadosMexico();

        assertNotNull(resultado);
        assertEquals(3, resultado.size());
        assertEquals("09", resultado.get(0).getClaveEntidad());
        assertEquals("CDMX", resultado.get(0).getClave());
        verify(mexicoGeografiaClient, times(1)).obtenerEstadosRaw();
    }

    @Test
    @DisplayName("Debe obtener todos los municipios agrupados por estado desde API externa")
    void testObtenerTodosLosMunicipios() {
        when(mexicoGeografiaClient.obtenerEstadosConMunicipiosRaw()).thenReturn(municipiosJsonMock);

        Map<String, List<String>> resultado = catalogoService.obtenerTodosLosMunicipios();

        assertNotNull(resultado);
        assertEquals(3, resultado.size());
        assertTrue(resultado.containsKey("Jalisco"));
        assertEquals(3, resultado.get("Jalisco").size());
        verify(mexicoGeografiaClient, times(1)).obtenerEstadosConMunicipiosRaw();
    }

    @Test
    @DisplayName("Debe buscar municipios por nombre de estado ignorando mayúsculas y acentos")
    void testObtenerMunicipiosPorEstado_PorNombre() {
        when(mexicoGeografiaClient.obtenerEstadosConMunicipiosRaw()).thenReturn(municipiosJsonMock);

        EstadoMunicipiosDto resultado = catalogoService.obtenerMunicipiosPorEstado("nuevo leon");

        assertNotNull(resultado);
        assertEquals("Nuevo León", resultado.getEstado());
        assertEquals(3, resultado.getTotalMunicipios());
        assertTrue(resultado.getMunicipios().contains("Monterrey"));
    }

    @Test
    @DisplayName("Debe buscar municipios por abreviatura o clave de estado (ej. CDMX)")
    void testObtenerMunicipiosPorEstado_PorClave() {
        when(mexicoGeografiaClient.obtenerEstadosConMunicipiosRaw()).thenReturn(municipiosJsonMock);
        when(mexicoGeografiaClient.obtenerEstadosRaw()).thenReturn(estadosJsonMock);

        EstadoMunicipiosDto resultado = catalogoService.obtenerMunicipiosPorEstado("CDMX");

        assertNotNull(resultado);
        assertEquals("Ciudad de México", resultado.getEstado());
        assertEquals(3, resultado.getTotalMunicipios());
        assertTrue(resultado.getMunicipios().contains("Benito Juárez"));
    }

    @Test
    @DisplayName("Debe lanzar ResourceNotFoundException si el estado no existe")
    void testObtenerMunicipiosPorEstado_NoEncontrado() {
        when(mexicoGeografiaClient.obtenerEstadosConMunicipiosRaw()).thenReturn(municipiosJsonMock);
        when(mexicoGeografiaClient.obtenerEstadosRaw()).thenReturn(estadosJsonMock);

        assertThrows(ResourceNotFoundException.class, () -> {
            catalogoService.obtenerMunicipiosPorEstado("EstadoInexistente");
        });
    }
}
