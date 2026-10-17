package com.proyecto.servicios.repositorys.sf;

import com.proyecto.servicios.entity.sf.Usuario;
import org.springframework.data.jpa.domain.Specification;

/**
 * Clase de utilidad para especificaciones dinámicas sobre Usuario.
 * Permite filtrar exclusivamente a través del RFC y la CURP del cliente vinculado.
 */
public final class UsuarioSpecification {

    private UsuarioSpecification() {
        // Constructor privado para clase utilitaria
    }

    /**
     * Predicado de búsqueda por RFC sobre el cliente asociado al usuario.
     * Soporta coincidencia exacta (12-13 caracteres) o búsqueda parcial case-insensitive.
     */
    public static Specification<Usuario> conRfc(String rfc) {
        return (root, query, cb) -> {
            if (rfc == null || rfc.isBlank()) {
                return cb.conjunction();
            }
            String valor = rfc.trim().toUpperCase();
            if (valor.length() == 12 || valor.length() == 13) {
                return cb.equal(root.get("cliente").get("rfc"), valor);
            }
            return cb.like(cb.upper(root.get("cliente").get("rfc")), "%" + valor + "%");
        };
    }

    /**
     * Predicado de búsqueda por CURP sobre el cliente asociado al usuario.
     * Soporta coincidencia exacta (18 caracteres) o búsqueda parcial case-insensitive.
     */
    public static Specification<Usuario> conCurp(String curp) {
        return (root, query, cb) -> {
            if (curp == null || curp.isBlank()) {
                return cb.conjunction();
            }
            String valor = curp.trim().toUpperCase();
            if (valor.length() == 18) {
                return cb.equal(root.get("cliente").get("curp"), valor);
            }
            return cb.like(cb.upper(root.get("cliente").get("curp")), "%" + valor + "%");
        };
    }
}
