package com.banquito.switchpagos.processing.mapper;

import com.banquito.switchpagos.processing.dto.api.ProcesarLoteResponse;
import com.banquito.switchpagos.processing.dto.api.ResultadoProcesamientoResponse;
import com.banquito.switchpagos.processing.enums.EstadoLineaPago;
import com.banquito.switchpagos.processing.repository.LineaPagoRepository;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.UUID;

@Component
public class ResultadoProcesamientoMapper {

    public ResultadoProcesamientoResponse toResultadoResponse(UUID uuidLote, LineaPagoRepository lineaPagoRepository) {
        Long totalLineas = lineaPagoRepository.countByLotePagoUuidLote(uuidLote);
        Long exitosas = lineaPagoRepository.countByLotePagoUuidLoteAndEstado(uuidLote, EstadoLineaPago.EXITOSA);
        Long rechazadas = lineaPagoRepository.countByLotePagoUuidLoteAndEstado(uuidLote, EstadoLineaPago.RECHAZADA);
        Long fallidas = lineaPagoRepository.countByLotePagoUuidLoteAndEstado(uuidLote, EstadoLineaPago.FALLIDA);
        BigDecimal montoExitoso = lineaPagoRepository.sumarMontoPorLoteUuidYEstado(uuidLote, EstadoLineaPago.EXITOSA);
        return new ResultadoProcesamientoResponse(totalLineas, exitosas, rechazadas, fallidas, montoExitoso);
    }

    public ProcesarLoteResponse toProcesarLoteResponse(UUID uuidLote, String estado,
                                                       ResultadoProcesamientoResponse resultado) {
        return new ProcesarLoteResponse(uuidLote, estado, resultado, "LIQUIDAR");
    }
}
