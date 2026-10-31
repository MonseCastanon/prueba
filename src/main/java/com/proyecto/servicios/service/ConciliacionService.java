package com.proyecto.servicios.service;

import com.proyecto.servicios.entity.sf.Transaccion;

public interface ConciliacionService {

    void conciliarTransaccion(Transaccion tx);
}

