package com.banquito.switchpagos.tarifaje.dto.api;

import java.math.BigDecimal;

public record MovimientoContableResponse(
        String concepto,
        BigDecimal monto,
        String estado
) {
}
