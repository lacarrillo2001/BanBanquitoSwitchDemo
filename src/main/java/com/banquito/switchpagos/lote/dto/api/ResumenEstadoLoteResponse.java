package com.banquito.switchpagos.lote.dto.api;

public record ResumenEstadoLoteResponse(
        Long totalLineas,
        Long pendientes,
        Long validadas,
        Long rechazadas
) {
}
