package com.banquito.switchpagos.pricing.dto.api;

import java.math.BigDecimal;

public record MovimientoContableResponse(
        String concepto,
        BigDecimal monto,
        String estado
) {
}
