package com.banquito.switchpagos.batch.mapper;

import com.banquito.switchpagos.batch.enums.EstadoLote;
import com.banquito.switchpagos.batch.model.HistorialEstadoLote;
import com.banquito.switchpagos.batch.model.LotePago;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;

@Component
public class HistorialEstadoLoteMapper {

    public HistorialEstadoLote toEntity(LotePago lotePago, EstadoLote estadoAnterior, EstadoLote estadoNuevo,
                                        String motivo, String cambiadoPor, OffsetDateTime fechaCambio) {
        HistorialEstadoLote historialEstadoLote = new HistorialEstadoLote();
        historialEstadoLote.setLotePago(lotePago);
        historialEstadoLote.setEstadoAnterior(estadoAnterior);
        historialEstadoLote.setEstadoNuevo(estadoNuevo);
        historialEstadoLote.setMotivo(motivo);
        historialEstadoLote.setCambiadoPor(cambiadoPor);
        historialEstadoLote.setFechaCambio(fechaCambio);
        return historialEstadoLote;
    }
}
