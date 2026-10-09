package com.proyecto.servicios.entity.sf;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.OffsetDateTime;

@Entity
@Table(name = "historial_bloqueo_cuenta")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HistorialBloqueoCuenta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cuenta_id", nullable = false)
    private Cuenta cuenta;

    @Column(name = "tipo_evento", nullable = false, length = 20)
    private String tipoEvento;

    @Column(name = "motivo", nullable = false, length = 255)
    private String motivo;

    @Column(name = "usuario_operador", nullable = false, length = 100)
    private String usuarioOperador;

    @CreationTimestamp
    @Column(name = "fecha_evento", nullable = false, updatable = false)
    private OffsetDateTime fechaEvento;
}
