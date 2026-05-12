package com.banquito.switchpagos.batch.dto.api;

public record ResumenEstadoLoteResponse(
        Long totalLineas,
        Long pendientes,
        Long validadas,
        Long rechazadas
) {
}
