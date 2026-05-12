package com.banquito.switchpagos.report.service;

import com.banquito.switchpagos.report.dto.api.ComprobanteLiquidacionResponse;
import com.banquito.switchpagos.report.dto.api.ReporteNovedadesResponse;
import com.banquito.switchpagos.report.enums.FormatoReporte;

import java.util.UUID;

public interface ReporteLoteService {

    ReporteNovedadesResponse obtenerOGenerarReporteNovedades(UUID uuidLote, FormatoReporte formato);

    ComprobanteLiquidacionResponse obtenerOGenerarComprobante(UUID uuidLote, FormatoReporte formato);
}
