package com.proyecto.servicios.entity.sf;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "cat_paises")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Pais {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "codigo_iso_alfa3", nullable = false, unique = true, length = 3)
    private String codigoIsoAlfa3;

    @Column(name = "nombre", nullable = false, length = 100)
    private String nombre;

    @Column(name = "activo", nullable = false)
    @Builder.Default
    private Boolean activo = true;
}
