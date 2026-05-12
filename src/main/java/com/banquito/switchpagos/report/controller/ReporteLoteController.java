package com.banquito.switchpagos.report.controller;

import com.banquito.switchpagos.report.dto.api.ComprobanteLiquidacionResponse;
import com.banquito.switchpagos.report.dto.api.ReporteNovedadesResponse;
import com.banquito.switchpagos.report.enums.FormatoReporte;
import com.banquito.switchpagos.report.service.ReporteLoteService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/pagos-masivos/lotes")
public class ReporteLoteController {

    private final ReporteLoteService reporteLoteService;

    public ReporteLoteController(ReporteLoteService reporteLoteService) {
        this.reporteLoteService = reporteLoteService;
    }

    @GetMapping("/{uuidLote}/novedades")
    public ReporteNovedadesResponse obtenerNovedades(
            @PathVariable("uuidLote") UUID uuidLote,
            @RequestParam(value = "formato", defaultValue = "JSON") FormatoReporte formato) {
        return reporteLoteService.obtenerOGenerarReporteNovedades(uuidLote, formato);
    }

    @GetMapping("/{uuidLote}/comprobante")
    public ComprobanteLiquidacionResponse obtenerComprobante(
            @PathVariable("uuidLote") UUID uuidLote,
            @RequestParam(value = "formato", defaultValue = "JSON") FormatoReporte formato) {
        return reporteLoteService.obtenerOGenerarComprobante(uuidLote, formato);
    }
}
