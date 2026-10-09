package com.proyecto.servicios.controller;

import com.proyecto.servicios.model.dto.EstadoDto;
import com.proyecto.servicios.model.dto.EstadoMunicipiosDto;
import com.proyecto.servicios.service.CatalogoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class CatalogoControllerTest {

    private MockMvc mockMvc;

    @Mock
    private CatalogoService catalogoService;

    @InjectMocks
    private CatalogoController catalogoController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(catalogoController).build();
    }

    @Test
    @DisplayName("GET /catalogos/estados debe devolver 200 OK y la lista de estados")
    void testConsultarEstadosMexico() throws Exception {
        List<EstadoDto> estados = Arrays.asList(
                new EstadoDto("09", "CDMX", "CIUDAD DE MÉXICO"),
                new EstadoDto("14", "Jal.", "JALISCO")
        );
        when(catalogoService.obtenerEstadosMexico()).thenReturn(estados);

        mockMvc.perform(get("/catalogos/estados").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].cve_ent").value("09"))
                .andExpect(jsonPath("$[0].clave").value("CDMX"))
                .andExpect(jsonPath("$[0].nombre").value("CIUDAD DE MÉXICO"));
    }

    @Test
    @DisplayName("GET /catalogos/municipios debe devolver 200 OK y todos los municipios")
    void testConsultarTodosLosMunicipios() throws Exception {
        Map<String, List<String>> municipios = new HashMap<>();
        municipios.put("Jalisco", List.of("Guadalajara", "Zapopan"));
        when(catalogoService.obtenerTodosLosMunicipios()).thenReturn(municipios);

        mockMvc.perform(get("/catalogos/municipios").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.Jalisco[0]").value("Guadalajara"));
    }

    @Test
    @DisplayName("GET /catalogos/estados/{estado}/municipios debe devolver 200 OK y municipios del estado")
    void testConsultarMunicipiosPorEstado() throws Exception {
        EstadoMunicipiosDto dto = EstadoMunicipiosDto.builder()
                .estado("Jalisco")
                .totalMunicipios(2)
                .municipios(List.of("Guadalajara", "Zapopan"))
                .build();
        when(catalogoService.obtenerMunicipiosPorEstado("Jalisco")).thenReturn(dto);

        mockMvc.perform(get("/catalogos/estados/Jalisco/municipios").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("Jalisco"))
                .andExpect(jsonPath("$.totalMunicipios").value(2))
                .andExpect(jsonPath("$.municipios[0]").value("Guadalajara"));
    }
}
