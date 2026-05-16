package com.banquito.switchpagos.pricing.service;

import com.banquito.switchpagos.pricing.dto.api.LiquidarLoteResponse;
import com.banquito.switchpagos.pricing.dto.internal.LiquidacionComprobanteInternalDto;
import com.banquito.switchpagos.pricing.dto.internal.MovimientoContableInternalDto;
import com.banquito.switchpagos.pricing.model.LiquidacionServicio;

import java.util.UUID;

public interface LiquidacionContableService {

    LiquidarLoteResponse liquidarServicio(UUID uuidLote);

    void registrarDetalleLiquidacion(LiquidacionServicio liquidacionServicio,
                                     MovimientoContableInternalDto movimientoContableInternalDto);

    LiquidacionComprobanteInternalDto obtenerLiquidacionCompletada(UUID uuidLote);
}
