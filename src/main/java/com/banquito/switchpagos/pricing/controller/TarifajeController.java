package com.banquito.switchpagos.pricing.controller;

import com.banquito.switchpagos.pricing.dto.api.LiquidarLoteResponse;
import com.banquito.switchpagos.pricing.dto.api.TarifaServicioResponse;
import com.banquito.switchpagos.pricing.service.LiquidacionContableService;
import com.banquito.switchpagos.pricing.service.TarifajeService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/pagos-masivos")
public class TarifajeController {

    private final TarifajeService tarifajeService;
    private final LiquidacionContableService liquidacionContableService;

    public TarifajeController(TarifajeService tarifajeService,
                              LiquidacionContableService liquidacionContableService) {
        this.tarifajeService = tarifajeService;
        this.liquidacionContableService = liquidacionContableService;
    }

    @PostMapping("/lotes/{uuidLote}/liquidar")
    public LiquidarLoteResponse liquidarLote(@PathVariable("uuidLote") UUID uuidLote) {
        return liquidacionContableService.liquidarServicio(uuidLote);
    }

    @GetMapping("/tarifas")
    public Object consultarTarifas(@RequestParam(value = "tipoServicio", required = false) String tipoServicio) {
        List<TarifaServicioResponse> tarifas = tarifajeService.consultarTarifasVigentes(tipoServicio);
        if (tipoServicio != null && !tipoServicio.isBlank() && !tarifas.isEmpty()) {
            return tarifas.getFirst();
        }
        return tarifas;
    }
}
