package com.proyecto.servicios.service;

import com.proyecto.servicios.model.*;

import java.time.OffsetDateTime;
import java.util.List;

public interface ClienteService {
    ClienteResponse registrarCliente(ClienteRegistroRequest request);
    ClienteResponse actualizarCliente(Long id, ClienteActualizaRequest request);
    ClienteResponse patchCliente(Long id, ClientePatchRequest request);
    GenericResponse bajaLogicaCliente(Long id);
    ClienteResponse consultarPorId(Long id);
    ClienteResponse consultarPorCurp(String curp);
    ClienteResponse consultarPorRfc(String rfc);
    ClienteResponse consultarPorNumeroCuenta(String numeroCuenta);
    List<ClienteResponse> consultarTodos();
    List<ClienteResponse> buscarPorFiltros(String nombre, String apePaterno, String apeMaterno, String curp, Boolean activo);
    List<ClienteResponse> buscarPorRangoFechas(OffsetDateTime inicio, OffsetDateTime fin);
}
