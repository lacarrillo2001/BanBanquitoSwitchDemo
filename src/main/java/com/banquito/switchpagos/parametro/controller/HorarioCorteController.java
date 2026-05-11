package com.banquito.switchpagos.parametro.controller;

import com.banquito.switchpagos.parametro.dto.api.HorarioCorteResponse;
import com.banquito.switchpagos.parametro.service.HorarioCorteService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/pagos-masivos/horarios-corte")
public class HorarioCorteController {

    private final HorarioCorteService horarioCorteService;

    public HorarioCorteController(HorarioCorteService horarioCorteService) {
        this.horarioCorteService = horarioCorteService;
    }

    @GetMapping
    public HorarioCorteResponse obtenerHorarioCorte() {
        return horarioCorteService.obtenerHorarioCorte();
    }
}
