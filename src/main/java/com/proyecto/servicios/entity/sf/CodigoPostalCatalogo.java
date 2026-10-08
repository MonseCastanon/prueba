package com.proyecto.servicios.entity.sf;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "cat_codigos_postales")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CodigoPostalCatalogo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "estado_id", nullable = false)
    private EstadoCatalogo estado;

    @Column(name = "codigo_postal", nullable = false, length = 5)
    private String codigoPostal;

    @Column(name = "municipio_alcaldia", nullable = false, length = 100)
    private String municipioAlcaldia;

    @Column(name = "colonia", nullable = false, length = 100)
    private String colonia;

    @Column(name = "activo", nullable = false)
    @Builder.Default
    private Boolean activo = true;
}
