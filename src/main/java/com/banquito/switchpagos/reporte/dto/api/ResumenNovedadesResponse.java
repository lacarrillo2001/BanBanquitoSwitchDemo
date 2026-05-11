package com.banquito.switchpagos.reporte.dto.api;

public record ResumenNovedadesResponse(
        Long totalLineas,
        Long exitosas,
        Long rechazadas,
        Long fallidas
) {
}
