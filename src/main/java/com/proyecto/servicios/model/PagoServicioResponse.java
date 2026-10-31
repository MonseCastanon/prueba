package com.proyecto.servicios.model;

import com.proyecto.servicios.entity.sf.Transaccion;
import com.proyecto.servicios.enums.EstadoTransaccion;
import com.proyecto.servicios.util.Masker;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Builder
@Schema(description = "Respuesta del procesamiento de pago de servicio o recarga")
public record PagoServicioResponse(
        @Schema(description = "Identificador único interno de la transacción")
        UUID idTransaccion,

        @Schema(description = "Llave de idempotencia del cliente")
        String idDocumento,

        @Schema(description = "Unique Payment Code enviado a Gestopago")
        String upc,

        @Schema(description = "Número de cuenta origen enmascarado")
        String numeroCuenta,

        @Schema(description = "ID de producto procesado")
        Integer idProducto,

        @Schema(description = "ID de servicio procesado")
        Integer idServicio,

        @Schema(description = "Referencia enmascarada")
        String referencia,

        @Schema(description = "Monto final cargado")
        BigDecimal monto,

        @Schema(description = "Estado de la transacción")
        EstadoTransaccion estado,

        @Schema(description = "Mensaje funcional para el usuario")
        String mensaje,

        @Schema(description = "Folio o número de autorización emitido por el switch externo")
        String numAutorizacion,

        @Schema(description = "Identificador de ticket o transacción externa")
        String idTx,

        @Schema(description = "Fecha de registro de la transacción")
        OffsetDateTime fecha
) {

    public static PagoServicioResponse exitosa(Transaccion tx, String numAutorizacion, String idTx) {
        return PagoServicioResponse.builder()
                .idTransaccion(tx.getId())
                .idDocumento(tx.getIdDocumento())
                .upc(tx.getUpc())
                .numeroCuenta(Masker.enmascararCuenta(tx.getCuenta().getNumeroCuenta()))
                .idProducto(tx.getIdProducto())
                .idServicio(tx.getIdServicio())
                .referencia(tx.getReferenciaEnmascarada())
                .monto(tx.getMonto())
                .estado(EstadoTransaccion.EXITOSA)
                .mensaje("Operación realizada con éxito")
                .numAutorizacion(numAutorizacion)
                .idTx(idTx)
                .fecha(tx.getFechaCreacion())
                .build();
    }

    public static PagoServicioResponse rechazada(Transaccion tx, String motivo) {
        return PagoServicioResponse.builder()
                .idTransaccion(tx.getId())
                .idDocumento(tx.getIdDocumento())
                .upc(tx.getUpc())
                .numeroCuenta(Masker.enmascararCuenta(tx.getCuenta().getNumeroCuenta()))
                .idProducto(tx.getIdProducto())
                .idServicio(tx.getIdServicio())
                .referencia(tx.getReferenciaEnmascarada())
                .monto(tx.getMonto())
                .estado(EstadoTransaccion.REVERSADA)
                .mensaje(motivo != null ? motivo : "La transacción fue rechazada por el switch y su saldo fue reembolsado.")
                .numAutorizacion(null)
                .idTx(null)
                .fecha(tx.getFechaCreacion())
                .build();
    }

    public static PagoServicioResponse enProceso(Transaccion tx) {
        return PagoServicioResponse.builder()
                .idTransaccion(tx.getId())
                .idDocumento(tx.getIdDocumento())
                .upc(tx.getUpc())
                .numeroCuenta(Masker.enmascararCuenta(tx.getCuenta().getNumeroCuenta()))
                .idProducto(tx.getIdProducto())
                .idServicio(tx.getIdServicio())
                .referencia(tx.getReferenciaEnmascarada())
                .monto(tx.getMonto())
                .estado(tx.getEstado())
                .mensaje("La transacción se encuentra en validación con el switch de servicios.")
                .numAutorizacion(tx.getAutorizacionProveedor())
                .idTx(tx.getIdTicketProveedor())
                .fecha(tx.getFechaCreacion())
                .build();
    }

    public static PagoServicioResponse desdeTransaccion(Transaccion tx) {
        return switch (tx.getEstado()) {
            case EXITOSA -> exitosa(tx, tx.getAutorizacionProveedor(), tx.getIdTicketProveedor());
            case REVERSADA, RECHAZADA -> rechazada(tx, tx.getCodigoProveedor());
            default -> enProceso(tx);
        };
    }
}

