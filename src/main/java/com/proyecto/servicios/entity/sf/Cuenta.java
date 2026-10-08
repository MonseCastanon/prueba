package com.proyecto.servicios.entity.sf;

import com.proyecto.servicios.enums.EstatusCuenta;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "cuentas")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Cuenta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    @Column(name = "numero_cuenta", nullable = false, unique = true, length = 20)
    private String numeroCuenta;

    @Column(name = "saldo", nullable = false, precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal saldo = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(name = "estatus", nullable = false, length = 20)
    @Builder.Default
    private EstatusCuenta estatus = EstatusCuenta.ACTIVA;

    @Column(name = "activa", nullable = false)
    @Builder.Default
    private Boolean activa = true;

    @Version
    @Column(name = "version_lock", nullable = false)
    private Long versionLock;

    @CreationTimestamp
    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private OffsetDateTime fechaCreacion;

    @UpdateTimestamp
    @Column(name = "fecha_actualizacion", nullable = false)
    private OffsetDateTime fechaActualizacion;

    @Column(name = "fecha_bloqueo")
    private OffsetDateTime fechaBloqueo;

    @Column(name = "motivo_bloqueo", length = 255)
    private String motivoBloqueo;

    @OneToMany(mappedBy = "cuenta", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<HistorialBloqueoCuenta> historialesBloqueo = new ArrayList<>();

    public void agregarEventoHistorial(String tipoEvento, String motivo, String operador) {
        HistorialBloqueoCuenta h = HistorialBloqueoCuenta.builder()
                .cuenta(this)
                .tipoEvento(tipoEvento)
                .motivo(motivo)
                .usuarioOperador(operador)
                .build();
        this.historialesBloqueo.add(h);
    }
}
