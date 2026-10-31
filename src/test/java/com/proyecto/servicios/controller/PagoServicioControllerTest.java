package com.proyecto.servicios.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.proyecto.servicios.config.security.AuthenticatedUser;
import com.proyecto.servicios.enums.EstadoTransaccion;
import com.proyecto.servicios.exception.GlobalExceptionHandler;
import com.proyecto.servicios.model.PagoServicioConsultaRequest;
import com.proyecto.servicios.model.PagoServicioRequest;
import com.proyecto.servicios.model.PagoServicioResponse;
import com.proyecto.servicios.service.PagoServicioService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class PagoServicioControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private PagoServicioService pagoServicioService;

    @Mock
    private com.proyecto.servicios.service.PinService pinService;

    @InjectMocks
    private PagoServicioController controller;

    private AuthenticatedUser authUser;
    private PagoServicioRequest requestValido;
    private PagoServicioResponse responseExitosa;

    @BeforeEach
    void setUp() {
        authUser = new AuthenticatedUser(
                1L, 1001L, "cliente@banco.com", "passHash", "ROLE_CLIENTE", Collections.emptyList(), true);

        // Resolver para inyectar automáticamente @AuthenticationPrincipal en standaloneSetup
        HandlerMethodArgumentResolver authPrincipalResolver = new HandlerMethodArgumentResolver() {
            @Override
            public boolean supportsParameter(MethodParameter parameter) {
                return parameter.hasParameterAnnotation(AuthenticationPrincipal.class);
            }

            @Override
            public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                          NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
                return authUser;
            }
        };

        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(authPrincipalResolver)
                .build();

        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        requestValido = new PagoServicioRequest(
                "9080512384", 5, 101, "DOC-12345678", "CONTRATO-5555", new BigDecimal("150.00"), "1234");

        responseExitosa = PagoServicioResponse.builder()
                .idTransaccion(UUID.randomUUID())
                .idDocumento("DOC-12345678")
                .upc("U000000100001")
                .numeroCuenta("****2384")
                .idProducto(101)
                .idServicio(5)
                .referencia("****5555")
                .monto(new BigDecimal("150.00"))
                .estado(EstadoTransaccion.EXITOSA)
                .mensaje("Operación realizada con éxito")
                .numAutorizacion("AUT-887766")
                .idTx("TX-5544")
                .fecha(OffsetDateTime.now())
                .build();
    }

    @Test
    @DisplayName("POST /pagos-servicios: Retorna 200 OK cuando la transacción es EXITOSA")
    void testPagarExitoso() throws Exception {
        when(pagoServicioService.pagar(any(PagoServicioRequest.class), any())).thenReturn(responseExitosa);

        mockMvc.perform(post("/pagos-servicios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestValido)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.mensaje").value("Operación realizada con éxito"))
                .andExpect(jsonPath("$.datos.estado").value("EXITOSA"))
                .andExpect(jsonPath("$.datos.numAutorizacion").value("AUT-887766"))
                .andExpect(jsonPath("$.datos.numeroCuenta").value("****2384"));

        verify(pagoServicioService, times(1)).pagar(any(PagoServicioRequest.class), any());
    }

    @Test
    @DisplayName("POST /pagos-servicios: Retorna 202 ACCEPTED cuando la transacción queda en proceso o INCIERTA")
    void testPagarEnProceso() throws Exception {
        PagoServicioResponse responseEnProceso = PagoServicioResponse.builder()
                .idTransaccion(UUID.randomUUID())
                .idDocumento("DOC-12345678")
                .upc("U000000100001")
                .numeroCuenta("****2384")
                .idProducto(101)
                .idServicio(5)
                .monto(new BigDecimal("150.00"))
                .estado(EstadoTransaccion.INCIERTA)
                .mensaje("La transacción se encuentra en validación con el switch de servicios.")
                .build();

        when(pagoServicioService.pagar(any(), any())).thenReturn(responseEnProceso);

        mockMvc.perform(post("/pagos-servicios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestValido)))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.status").value(202))
                .andExpect(jsonPath("$.datos.estado").value("INCIERTA"));

        verify(pagoServicioService, times(1)).pagar(any(), any());
    }

    @Test
    @DisplayName("POST /pagos-servicios: Retorna 422 UNPROCESSABLE ENTITY cuando la transacción es REVERSADA")
    void testPagarReversada() throws Exception {
        PagoServicioResponse responseReversada = PagoServicioResponse.builder()
                .idTransaccion(UUID.randomUUID())
                .idDocumento("DOC-12345678")
                .upc("U000000100001")
                .numeroCuenta("****2384")
                .idProducto(101)
                .idServicio(5)
                .monto(new BigDecimal("150.00"))
                .estado(EstadoTransaccion.REVERSADA)
                .mensaje("Rechazado por switch")
                .build();

        when(pagoServicioService.pagar(any(), any())).thenReturn(responseReversada);

        mockMvc.perform(post("/pagos-servicios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestValido)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.status").value(422))
                .andExpect(jsonPath("$.datos.estado").value("REVERSADA"));

        verify(pagoServicioService, times(1)).pagar(any(), any());
    }

    @Test
    @DisplayName("POST /pagos-servicios: Retorna 400 BAD REQUEST ante campos inválidos en el DTO")
    void testPagarCamposInvalidos() throws Exception {
        PagoServicioRequest requestInvalido = new PagoServicioRequest(
                "123", // Muy corto para número de cuenta
                -1,    // Negativo
                null,  // Nulo
                "DOC", // Muy corto para idDocumento (min 8)
                "",    // Vacía
                new BigDecimal("-5.00"), // Negativo
                "abc"  // No numérico
        );

        mockMvc.perform(post("/pagos-servicios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestInvalido)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.detalles").isMap());

        verify(pagoServicioService, never()).pagar(any(), any());
    }

    @Test
    @DisplayName("POST /pagos-servicios/consulta: Retorna 200 OK con el estado de la transacción")
    void testConsultarExitoso() throws Exception {
        PagoServicioConsultaRequest requestConsulta = new PagoServicioConsultaRequest("DOC-12345678", "9080512384");

        when(pagoServicioService.consultar(any(), any())).thenReturn(responseExitosa);

        mockMvc.perform(post("/pagos-servicios/consulta")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestConsulta)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.datos.idDocumento").value("DOC-12345678"));

        verify(pagoServicioService, times(1)).consultar(any(), any());
    }

    @Test
    @DisplayName("PUT /pagos-servicios/pin: Retorna 200 OK al configurar el PIN transaccional")
    void testConfigurarPinExitoso() throws Exception {
        com.proyecto.servicios.model.PinConfiguracionRequest reqPin = new com.proyecto.servicios.model.PinConfiguracionRequest("4829");

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put("/pagos-servicios/pin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reqPin)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.datos.pinConfigurado").value(true));

        verify(pinService, times(1)).asignarPin(1L, "4829");
    }
}

