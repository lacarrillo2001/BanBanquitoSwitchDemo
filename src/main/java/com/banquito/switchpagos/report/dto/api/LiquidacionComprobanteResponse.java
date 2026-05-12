package com.banquito.switchpagos.report.dto.api;

import java.math.BigDecimal;

public record LiquidacionComprobanteResponse(
        BigDecimal tarifaUnitariaAplicada,
        BigDecimal subtotalComision,
        BigDecimal ivaPorcentajeAplicado,
        BigDecimal montoIva,
        BigDecimal totalDebitado
) {
}
