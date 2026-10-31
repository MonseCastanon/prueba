package com.proyecto.servicios.service.validation;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class LimitesPago {

    private final BigDecimal minimo;
    private final BigDecimal maximoPorOperacion;
    private final BigDecimal maximoDiario;

    public LimitesPago(
            @Value("${banco.limites.monto-minimo:1.00}") BigDecimal minimo,
            @Value("${banco.limites.monto-maximo-operacion:10000.00}") BigDecimal maximoPorOperacion,
            @Value("${banco.limites.monto-maximo-diario:50000.00}") BigDecimal maximoDiario) {
        this.minimo = minimo;
        this.maximoPorOperacion = maximoPorOperacion;
        this.maximoDiario = maximoDiario;
    }

    public BigDecimal minimo() {
        return minimo;
    }

    public BigDecimal maximoPorOperacion() {
        return maximoPorOperacion;
    }

    public BigDecimal maximoDiario() {
        return maximoDiario;
    }
}

