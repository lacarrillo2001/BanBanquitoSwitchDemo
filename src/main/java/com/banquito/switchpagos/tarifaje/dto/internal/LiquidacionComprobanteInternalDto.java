package com.banquito.switchpagos.tarifaje.dto.internal;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record LiquidacionComprobanteInternalDto(
        Integer transaccionesExitosas,
        Integer transaccionesFallidas,
        BigDecimal tarifaUnitariaAplicada,
        BigDecimal ivaPorcentajeAplicado,
        BigDecimal subtotalComision,
        BigDecimal montoIva,
        BigDecimal totalDebitado,
        OffsetDateTime fechaLiquidacion
) {
}
