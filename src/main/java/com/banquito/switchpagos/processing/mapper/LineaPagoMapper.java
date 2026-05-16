package com.banquito.switchpagos.processing.mapper;

import com.banquito.switchpagos.file.dto.internal.DetalleArchivoPagoInternalDto;
import com.banquito.switchpagos.batch.dto.api.LineaPagoResponse;
import com.banquito.switchpagos.batch.model.LotePago;
import com.banquito.switchpagos.processing.dto.internal.LineaPagoInternalDto;
import com.banquito.switchpagos.processing.enums.EstadoLineaPago;
import com.banquito.switchpagos.processing.model.LineaPago;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class LineaPagoMapper {

    public LineaPago toEntity(LotePago lotePago, DetalleArchivoPagoInternalDto detalle) {
        LineaPago lineaPago = new LineaPago();
        lineaPago.setLotePago(lotePago);
        lineaPago.setSecuencial(detalle.secuencial());
        lineaPago.setIdentificacionBeneficiario(detalle.identificacionBeneficiario());
        lineaPago.setNombreBeneficiario(detalle.nombreBeneficiario());
        lineaPago.setCuentaDestino(detalle.cuentaDestino());
        lineaPago.setMonto(detalle.monto());
        lineaPago.setConceptoReferencia(detalle.conceptoReferencia());
        lineaPago.setCorreoNotificacion(detalle.correoNotificacion());
        lineaPago.setEstado(EstadoLineaPago.PENDIENTE);
        lineaPago.setUuidOperacionSwitch(UUID.randomUUID());
        return lineaPago;
    }

    public LineaPagoInternalDto toInternalDto(LineaPago lineaPago) {
        return new LineaPagoInternalDto(
                lineaPago.getIdLinea(),
                lineaPago.getUuidOperacionSwitch(),
                lineaPago.getSecuencial(),
                lineaPago.getIdentificacionBeneficiario(),
                lineaPago.getNombreBeneficiario(),
                lineaPago.getCuentaDestino(),
                lineaPago.getMonto(),
                lineaPago.getConceptoReferencia(),
                lineaPago.getCorreoNotificacion(),
                lineaPago.getEstado() != null ? lineaPago.getEstado().name() : null,
                lineaPago.getCodigoError(),
                lineaPago.getMensajeError(),
                lineaPago.getFechaValidacion()
        );
    }

    public LineaPagoResponse toResponse(LineaPagoInternalDto lineaPagoInternalDto) {
        return new LineaPagoResponse(
                lineaPagoInternalDto.uuidOperacionSwitch(),
                lineaPagoInternalDto.secuencial(),
                lineaPagoInternalDto.identificacionBeneficiario(),
                lineaPagoInternalDto.nombreBeneficiario(),
                lineaPagoInternalDto.cuentaDestino(),
                lineaPagoInternalDto.monto(),
                lineaPagoInternalDto.conceptoReferencia(),
                lineaPagoInternalDto.correoNotificacion(),
                lineaPagoInternalDto.estado(),
                lineaPagoInternalDto.codigoError(),
                lineaPagoInternalDto.mensajeError(),
                lineaPagoInternalDto.fechaValidacion()
        );
    }
}
