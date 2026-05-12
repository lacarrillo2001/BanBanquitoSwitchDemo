package com.banquito.switchpagos.catalog.controller;

import com.banquito.switchpagos.catalog.dto.api.TipoServicioResponse;
import com.banquito.switchpagos.catalog.service.TipoServicioService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/pagos-masivos/tipos-servicio")
public class TipoServicioController {

    private final TipoServicioService tipoServicioService;

    public TipoServicioController(TipoServicioService tipoServicioService) {
        this.tipoServicioService = tipoServicioService;
    }

    @GetMapping
    public List<TipoServicioResponse> listarActivos() {
        return tipoServicioService.listarActivos();
    }
}
