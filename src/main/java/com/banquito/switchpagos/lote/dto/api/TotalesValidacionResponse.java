package com.banquito.switchpagos.lote.dto.api;

import java.math.BigDecimal;

public record TotalesValidacionResponse(
        Integer totalRegistrosDeclarado,
        Integer totalRegistrosPie,
        Long totalLineasParseadas,
        BigDecimal montoTotalDeclarado,
        BigDecimal montoTotalPie,
        BigDecimal montoTotalDetalle
) {
}
