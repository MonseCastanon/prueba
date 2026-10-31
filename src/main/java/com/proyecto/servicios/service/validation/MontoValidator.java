package com.proyecto.servicios.service.validation;

import com.proyecto.servicios.exception.InvalidPaymentException;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
public class MontoValidator {

    public BigDecimal resolver(ReglaMonto regla, BigDecimal solicitado) {
        if (regla == null) {
            throw new InvalidPaymentException("Regla de monto no especificada");
        }

        if (regla instanceof ReglaMonto.PrecioFijo f) {
            if (solicitado != null && solicitado.compareTo(f.valor()) != 0) {
                throw new InvalidPaymentException("El monto enviado (" + solicitado + ") no corresponde al precio fijo del producto (" + f.valor() + ")");
            }
            return f.valor().setScale(2, RoundingMode.HALF_UP);
        } else if (regla instanceof ReglaMonto.MontoLibre l) {
            if (solicitado == null) {
                throw new InvalidPaymentException("El monto es obligatorio para pagos de servicios");
            }
            if (solicitado.compareTo(BigDecimal.ZERO) <= 0) {
                throw new InvalidPaymentException("El monto a pagar debe ser mayor a cero");
            }
            if (solicitado.scale() > 2) {
                throw new InvalidPaymentException("El monto no puede tener más de dos decimales");
            }
            if (solicitado.compareTo(l.min()) < 0) {
                throw new InvalidPaymentException("El monto mínimo permitido es $" + l.min());
            }
            if (solicitado.compareTo(l.max()) > 0) {
                throw new InvalidPaymentException("El monto excede el tope máximo por operación de $" + l.max());
            }
            return solicitado.setScale(2, RoundingMode.HALF_UP);
        }

        throw new InvalidPaymentException("Tipo de regla de monto no reconocida");
    }
}
