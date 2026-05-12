package com.banquito.switchpagos.processing.dto.api;

import java.math.BigDecimal;

public record ResultadoProcesamientoResponse(
        Long totalLineas,
        Long exitosas,
        Long rechazadas,
        Long fallidas,
        BigDecimal montoProcesadoExitoso
) {
}
