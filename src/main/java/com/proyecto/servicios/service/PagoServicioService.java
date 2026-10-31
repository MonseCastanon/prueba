package com.proyecto.servicios.service;

import com.proyecto.servicios.config.security.AuthenticatedUser;
import com.proyecto.servicios.model.PagoServicioConsultaRequest;
import com.proyecto.servicios.model.PagoServicioRequest;
import com.proyecto.servicios.model.PagoServicioResponse;

public interface PagoServicioService {

    PagoServicioResponse pagar(PagoServicioRequest request, AuthenticatedUser user);

    PagoServicioResponse consultar(PagoServicioConsultaRequest request, AuthenticatedUser user);
}

