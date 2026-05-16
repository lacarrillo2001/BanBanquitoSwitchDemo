package com.banquito.switchpagos.batch.controller;

import com.banquito.switchpagos.batch.dto.api.ProcesarPendientesColaResponse;
import com.banquito.switchpagos.batch.service.ColaProcesamientoService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/pagos-masivos/cola")
public class ColaProcesamientoController {

    private final ColaProcesamientoService colaProcesamientoService;

    public ColaProcesamientoController(ColaProcesamientoService colaProcesamientoService) {
        this.colaProcesamientoService = colaProcesamientoService;
    }

    @PostMapping("/procesar-pendientes")
    public ProcesarPendientesColaResponse procesarPendientes() {
        return colaProcesamientoService.procesarPendientesManual();
    }
}
