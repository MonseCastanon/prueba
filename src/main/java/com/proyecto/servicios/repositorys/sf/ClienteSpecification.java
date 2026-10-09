package com.proyecto.servicios.repositorys.sf;

import com.proyecto.servicios.entity.sf.Cliente;
import org.springframework.data.jpa.domain.Specification;

public class ClienteSpecification {

    public static Specification<Cliente> conNombre(String nombre) {
        return (root, query, cb) -> (nombre == null || nombre.isBlank()) ?
                cb.conjunction() : cb.like(cb.lower(root.get("nombre")), "%" + nombre.trim().toLowerCase() + "%");
    }

    public static Specification<Cliente> conApellidoPaterno(String apePaterno) {
        return (root, query, cb) -> (apePaterno == null || apePaterno.isBlank()) ?
                cb.conjunction() : cb.like(cb.lower(root.get("apellidoPaterno")), "%" + apePaterno.trim().toLowerCase() + "%");
    }

    public static Specification<Cliente> conApellidoMaterno(String apeMaterno) {
        return (root, query, cb) -> (apeMaterno == null || apeMaterno.isBlank()) ?
                cb.conjunction() : cb.like(cb.lower(root.get("apellidoMaterno")), "%" + apeMaterno.trim().toLowerCase() + "%");
    }

    public static Specification<Cliente> conCurp(String curp) {
        return (root, query, cb) -> (curp == null || curp.isBlank()) ?
                cb.conjunction() : cb.equal(root.get("curp"), curp.trim().toUpperCase());
    }

    public static Specification<Cliente> esActivo(Boolean activo) {
        return (root, query, cb) -> activo == null ?
                cb.conjunction() : cb.equal(root.get("activo"), activo);
    }
}
