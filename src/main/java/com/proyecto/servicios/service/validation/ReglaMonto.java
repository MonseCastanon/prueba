package com.proyecto.servicios.service.validation;

import com.proyecto.servicios.entity.mongo.CatProductoDocument;
import com.proyecto.servicios.exception.InvalidPaymentException;

import java.math.BigDecimal;

public sealed interface ReglaMonto {

    record PrecioFijo(BigDecimal valor) implements ReglaMonto { }

    record MontoLibre(BigDecimal min, BigDecimal max) implements ReglaMonto { }

    static ReglaMonto desde(CatProductoDocument p, LimitesPago limites) {
        if (p == null || p.getTipoFront() == null) {
            throw new InvalidPaymentException("Producto inválido o tipoFront no definido");
        }
        return switch (p.getTipoFront()) {
            case 1 -> {
                if (p.getPrecio() == null || p.getPrecio().isBlank()) {
                    throw new InvalidPaymentException("El producto no posee precio configurado en el catálogo");
                }
                try {
                    yield new PrecioFijo(new BigDecimal(p.getPrecio().trim()));
                } catch (NumberFormatException e) {
                    throw new InvalidPaymentException("El precio en catálogo tiene formato numérico inválido");
                }
            }
            case 2 -> new MontoLibre(limites.minimo(), limites.maximoPorOperacion());
            default -> throw new InvalidPaymentException("Tipo de producto no soportado para este canal (tipoFront=" + p.getTipoFront() + ")");
        };
    }
}

