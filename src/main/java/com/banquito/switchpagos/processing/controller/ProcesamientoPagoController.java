package com.banquito.switchpagos.processing.controller;

import com.banquito.switchpagos.processing.dto.api.ProcesarLoteRequest;
import com.banquito.switchpagos.processing.dto.api.ProcesarLoteResponse;
import com.banquito.switchpagos.processing.service.ProcesamientoPagoService;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/pagos-masivos/lotes")
public class ProcesamientoPagoController {

    private final ProcesamientoPagoService procesamientoPagoService;

    public ProcesamientoPagoController(ProcesamientoPagoService procesamientoPagoService) {
        this.procesamientoPagoService = procesamientoPagoService;
    }

    @PostMapping("/{uuidLote}/procesar")
    public ProcesarLoteResponse procesarLote(@PathVariable("uuidLote") UUID uuidLote,
                                             @RequestBody(required = false) ProcesarLoteRequest procesarLoteRequest) {
        return procesamientoPagoService.procesarLote(uuidLote, procesarLoteRequest);
    }
}
