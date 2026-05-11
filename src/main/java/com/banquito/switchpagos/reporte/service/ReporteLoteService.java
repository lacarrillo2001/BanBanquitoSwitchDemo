package com.banquito.switchpagos.reporte.service;

import com.banquito.switchpagos.reporte.dto.api.ComprobanteLiquidacionResponse;
import com.banquito.switchpagos.reporte.dto.api.ReporteNovedadesResponse;
import com.banquito.switchpagos.reporte.enums.FormatoReporte;

import java.util.UUID;

public interface ReporteLoteService {

    ReporteNovedadesResponse obtenerOGenerarReporteNovedades(UUID uuidLote, FormatoReporte formato);

    ComprobanteLiquidacionResponse obtenerOGenerarComprobante(UUID uuidLote, FormatoReporte formato);
}
