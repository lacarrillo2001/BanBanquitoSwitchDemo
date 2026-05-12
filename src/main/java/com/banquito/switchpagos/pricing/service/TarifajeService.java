package com.banquito.switchpagos.pricing.service;

import com.banquito.switchpagos.pricing.dto.api.TarifaServicioResponse;
import com.banquito.switchpagos.pricing.dto.internal.CalculoLiquidacionInternalDto;
import com.banquito.switchpagos.pricing.model.TarifaServicio;

import java.util.List;
import java.util.UUID;

public interface TarifajeService {

    List<TarifaServicioResponse> consultarTarifasVigentes(String tipoServicio);

    TarifaServicio calcularTarifaAplicable(String tipoServicio, Integer transaccionesExitosas);

    CalculoLiquidacionInternalDto calcularLiquidacion(UUID uuidLote);
}
