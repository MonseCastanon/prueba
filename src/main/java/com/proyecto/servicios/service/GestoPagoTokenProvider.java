package com.proyecto.servicios.service;

public interface GestoPagoTokenProvider {

    String tokenVigente();

    void asegurarTokenVigente();

    void invalidarToken();
}

