package com.proyecto.servicios.entity.sf;

import com.proyecto.servicios.enums.EstadoTransaccion;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "transacciones_servicio")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Transaccion {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cuenta_id", nullable = false)
    private Cuenta cuenta;

    @Column(name = "id_documento", nullable = false, length = 40)
    private String idDocumento;

    @Column(name = "upc", nullable = false, length = 15, unique = true)
    private String upc;

    @Column(name = "hora_local", nullable = false, length = 14)
    private String horaLocal;

    @Column(name = "request_hash", nullable = false, length = 64)
    private String requestHash;

    @Column(name = "id_producto", nullable = false)
    private Integer idProducto;

    @Column(name = "id_servicio", nullable = false)
    private Integer idServicio;

    @Column(name = "referencia_enmascarada", nullable = false, length = 30)
    private String referenciaEnmascarada;

    @Column(name = "payload_cifrado", columnDefinition = "TEXT")
    private String payloadCifrado;

    @Column(name = "monto", nullable = false, precision = 15, scale = 2)
    private BigDecimal monto;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 25)
    private EstadoTransaccion estado;

    @Column(name = "codigo_proveedor", length = 20)
    private String codigoProveedor;

    @Column(name = "autorizacion_proveedor", length = 60)
    private String autorizacionProveedor;

    @Column(name = "id_ticket_proveedor", length = 60)
    private String idTicketProveedor;

    @Column(name = "intentos_conciliacion", nullable = false)
    @Builder.Default
    private Short intentosConciliacion = 0;

    @Column(name = "proximo_intento_en")
    private OffsetDateTime proximoIntentoEn;

    @CreationTimestamp
    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private OffsetDateTime fechaCreacion;

    @Column(name = "fecha_envio")
    private OffsetDateTime fechaEnvio;

    @Column(name = "fecha_resolucion")
    private OffsetDateTime fechaResolucion;

    @Version
    @Column(name = "version_lock", nullable = false)
    @Builder.Default
    private Long versionLock = 0L;
}
