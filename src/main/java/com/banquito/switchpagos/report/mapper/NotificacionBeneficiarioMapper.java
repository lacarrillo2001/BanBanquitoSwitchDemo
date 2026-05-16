package com.banquito.switchpagos.report.mapper;

import com.banquito.switchpagos.processing.dto.internal.LineaPagoInternalDto;
import com.banquito.switchpagos.processing.model.LineaPago;
import com.banquito.switchpagos.report.enums.EstadoEnvioNotificacion;
import com.banquito.switchpagos.report.enums.TipoNotificacion;
import com.banquito.switchpagos.report.model.NotificacionBeneficiario;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;

@Component
public class NotificacionBeneficiarioMapper {

    public NotificacionBeneficiario toEntity(LineaPago lineaPago, LineaPagoInternalDto lineaPagoInternalDto,
                                             ObjectNode contenido, Boolean correoValido,
                                             OffsetDateTime fechaActualizacion) {
        NotificacionBeneficiario notificacion = new NotificacionBeneficiario();
        notificacion.setLineaPago(lineaPago);
        notificacion.setCorreoDestino(lineaPagoInternalDto.correoNotificacion());
        notificacion.setTipoNotificacion(TipoNotificacion.PAGO_EXITOSO);
        notificacion.setAsunto("Pago recibido Banco BanQuito");
        notificacion.setContenido(contenido);
        notificacion.setEstadoEnvio(Boolean.TRUE.equals(correoValido)
                ? EstadoEnvioNotificacion.PENDIENTE
                : EstadoEnvioNotificacion.ERROR);
        notificacion.setErrorEnvio(Boolean.TRUE.equals(correoValido)
                ? null
                : "Correo de notificacion invalido.");
        notificacion.setReintentos(0);
        notificacion.setFechaActualizacion(fechaActualizacion);
        return notificacion;
    }
}
