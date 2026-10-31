package com.proyecto.servicios.service.validation;

import com.proyecto.servicios.entity.mongo.CatProductoDocument;
import com.proyecto.servicios.exception.InvalidPaymentException;
import com.proyecto.servicios.repositorys.mongo.CatProductoMongoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CatalogoPagoValidator {

    private final CatProductoMongoRepository mongoRepository;

    public CatProductoDocument validar(Integer idProducto, Integer idServicio, String referencia) {
        if (idProducto == null || idServicio == null) {
            throw new InvalidPaymentException("El idProducto y el idServicio son obligatorios");
        }

        CatProductoDocument producto = mongoRepository.findByIdProducto(idProducto)
                .orElseThrow(() -> new InvalidPaymentException("Producto con ID " + idProducto + " no encontrado en el catálogo"));

        if (!Boolean.TRUE.equals(producto.getActivo())) {
            throw new InvalidPaymentException("El producto " + idProducto + " no se encuentra activo");
        }

        if (!idServicio.equals(producto.getIdServicio())) {
            throw new InvalidPaymentException("El idServicio " + idServicio + " no coincide con el configurado para el producto " + idProducto);
        }

        Integer tipoFront = producto.getTipoFront();
        if (tipoFront == null || (tipoFront != 1 && tipoFront != 2)) {
            throw new InvalidPaymentException("Tipo de front no soportado para este canal de pago: " + tipoFront);
        }

        validarFormatoReferencia(referencia, producto.getTipoReferencia());

        return producto;
    }

    private void validarFormatoReferencia(String referencia, String tipoReferencia) {
        if (referencia == null || referencia.isBlank()) {
            throw new InvalidPaymentException("La referencia o teléfono es obligatoria");
        }

        String refLimpia = referencia.trim();

        if (tipoReferencia == null || tipoReferencia.isBlank()) {
            if (refLimpia.length() < 3 || refLimpia.length() > 100) {
                throw new InvalidPaymentException("La longitud de la referencia no es válida");
            }
            return;
        }

        String tipo = tipoReferencia.toLowerCase().trim();

        if (tipo.contains("a") && !tipo.contains("b") && !tipo.contains("c")) {
            // Teléfono a 10 dígitos
            if (!refLimpia.matches("\\d{10}")) {
                throw new InvalidPaymentException("La referencia debe ser un número telefónico de 10 dígitos");
            }
        } else if (tipo.contains("c") && !tipo.contains("a") && !tipo.contains("b")) {
            // Código de barras numérico
            if (!refLimpia.matches("\\d{3,100}")) {
                throw new InvalidPaymentException("La referencia debe ser un código numérico");
            }
        } else {
            // Alfanumérico (referencia tipo 'b' o combinaciones 'ab', 'bc')
            if (!refLimpia.matches("[A-Za-z0-9\\-_#. ]{1,100}")) {
                throw new InvalidPaymentException("La referencia contiene caracteres no permitidos");
            }
        }
    }
}

