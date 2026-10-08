package com.proyecto.servicios.entity.sf;

import com.proyecto.servicios.enums.EstatusFacial;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(name = "biometria_cliente")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BiometriaCliente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id", nullable = false, unique = true)
    private Cliente cliente;

    @Basic(fetch = FetchType.LAZY)
    @Column(name = "foto_rostro")
    private byte[] fotoRostro;

    @Column(name = "sha256_foto", length = 64)
    private String sha256Foto;

    @Column(name = "vector_facial", columnDefinition = "TEXT")
    private String vectorFacial;

    @Column(name = "liveness_score", precision = 5, scale = 4)
    private BigDecimal livenessScore;

    @Enumerated(EnumType.STRING)
    @Column(name = "estatus_facial", nullable = false, length = 20)
    @Builder.Default
    private EstatusFacial estatusFacial = EstatusFacial.APROBADO;

    @Basic(fetch = FetchType.LAZY)
    @Column(name = "template_dactilar")
    private byte[] templateDactilar;

    @CreationTimestamp
    @Column(name = "fecha_captura", nullable = false, updatable = false)
    private OffsetDateTime fechaCaptura;

    @UpdateTimestamp
    @Column(name = "fecha_actualizacion", nullable = false)
    private OffsetDateTime fechaActualizacion;
}
