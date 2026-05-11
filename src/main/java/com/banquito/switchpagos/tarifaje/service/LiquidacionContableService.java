package com.banquito.switchpagos.tarifaje.service;

import com.banquito.switchpagos.tarifaje.dto.api.LiquidarLoteResponse;
import com.banquito.switchpagos.tarifaje.dto.internal.LiquidacionComprobanteInternalDto;
import com.banquito.switchpagos.tarifaje.dto.internal.MovimientoContableInternalDto;
import com.banquito.switchpagos.tarifaje.model.LiquidacionServicio;

import java.util.UUID;

public interface LiquidacionContableService {

    LiquidarLoteResponse liquidarServicio(UUID uuidLote);

    void registrarDetalleLiquidacion(LiquidacionServicio liquidacionServicio,
                                     MovimientoContableInternalDto movimientoContableInternalDto);

    LiquidacionComprobanteInternalDto obtenerLiquidacionCompletada(UUID uuidLote);
}
