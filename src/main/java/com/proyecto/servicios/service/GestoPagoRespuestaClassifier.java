package com.proyecto.servicios.service;

import com.proyecto.servicios.enums.AccionTransaccion;
import com.proyecto.servicios.model.gestopago.xml.GestoPagoConfirmTxResponse;
import com.proyecto.servicios.model.gestopago.xml.GestoPagoSendTxResponse;
import org.springframework.stereotype.Component;

@Component
public class GestoPagoRespuestaClassifier {

    public AccionTransaccion clasificarSendTx(GestoPagoSendTxResponse resp) {
        if (resp == null || resp.getMensaje() == null || resp.getMensaje().getCodigo() == null) {
            return AccionTransaccion.MARCAR_INCIERTA_PARA_CONCILIACION;
        }

        String codigo = resp.getMensaje().getCodigo().trim();

        // Código "01" o "1" = Operación realizada con éxito
        if ("01".equals(codigo) || "1".equals(codigo)) {
            return AccionTransaccion.CONFIRMAR_EXITO;
        }

        // Código "02" = Saldo insuficiente en la cuenta mayorista de Gestopago (no se aplicó)
        if ("02".equals(codigo)) {
            return AccionTransaccion.REVERSAR_SALDO;
        }

        // Código "06" = Transacción aplicada anteriormente / Duplicado detectado
        // Código "82" = Tiempo de espera alcanzado en switch externo
        if ("06".equals(codigo) || "82".equals(codigo)) {
            return AccionTransaccion.MARCAR_INCIERTA_PARA_CONCILIACION;
        }

        // Cualquier otro código no documentado: prudencia financiera
        return AccionTransaccion.MARCAR_INCIERTA_PARA_CONCILIACION;
    }

    public AccionTransaccion clasificarConfirmTx(GestoPagoConfirmTxResponse resp) {
        if (resp == null || resp.getMensaje() == null || resp.getMensaje().getCodigo() == null) {
            return AccionTransaccion.MARCAR_INCIERTA_PARA_CONCILIACION;
        }

        String codigo = resp.getMensaje().getCodigo().trim();

        // En confirmTx.do: "06" significa "La transacción fue aplicada exitosamente"
        if ("06".equals(codigo)) {
            return AccionTransaccion.CONFIRMAR_EXITO;
        }

        // En confirmTx.do: "70" significa "La transacción NO fue aplicada"
        if ("70".equals(codigo)) {
            return AccionTransaccion.REVERSAR_SALDO;
        }

        return AccionTransaccion.MARCAR_INCIERTA_PARA_CONCILIACION;
    }
}

