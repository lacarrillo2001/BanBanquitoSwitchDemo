package com.banquito.switchpagos.tarifaje.service;

import com.banquito.switchpagos.tarifaje.dto.api.TarifaServicioResponse;
import com.banquito.switchpagos.tarifaje.dto.internal.CalculoLiquidacionInternalDto;
import com.banquito.switchpagos.tarifaje.model.TarifaServicio;

import java.util.List;
import java.util.UUID;

public interface TarifajeService {

    List<TarifaServicioResponse> consultarTarifasVigentes(String tipoServicio);

    TarifaServicio calcularTarifaAplicable(String tipoServicio, Integer transaccionesExitosas);

    CalculoLiquidacionInternalDto calcularLiquidacion(UUID uuidLote);
}
