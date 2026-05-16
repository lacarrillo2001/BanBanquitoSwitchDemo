package com.banquito.switchpagos.pricing.mapper;

import com.banquito.switchpagos.pricing.dto.api.RangoTarifaResponse;
import com.banquito.switchpagos.pricing.dto.api.TarifaServicioResponse;
import com.banquito.switchpagos.pricing.model.TarifaServicio;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class TarifaServicioMapper {

    public RangoTarifaResponse toRangoResponse(TarifaServicio tarifaServicio) {
        return new RangoTarifaResponse(
                tarifaServicio.getRangoDesde(),
                tarifaServicio.getRangoHasta(),
                tarifaServicio.getTarifaUnitaria()
        );
    }

    public TarifaServicioResponse toResponse(String tipoServicio, List<TarifaServicio> tarifas) {
        TarifaServicio primeraTarifa = tarifas.getFirst();
        List<RangoTarifaResponse> rangos = tarifas.stream()
                .map(this::toRangoResponse)
                .toList();
        return new TarifaServicioResponse(
                tipoServicio,
                primeraTarifa.getMoneda(),
                primeraTarifa.getVigenteDesde(),
                rangos
        );
    }
}
