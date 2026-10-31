package com.proyecto.servicios.service;

import com.proyecto.servicios.config.security.AuthenticatedUser;

public interface PinService {

    void validarPin(AuthenticatedUser user, String pin);

    void asignarPin(Long usuarioId, String pin);
}

