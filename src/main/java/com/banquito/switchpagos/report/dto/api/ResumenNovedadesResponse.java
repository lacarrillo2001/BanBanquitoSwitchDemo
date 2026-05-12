package com.banquito.switchpagos.report.dto.api;

public record ResumenNovedadesResponse(
        Long totalLineas,
        Long exitosas,
        Long rechazadas,
        Long fallidas
) {
}
