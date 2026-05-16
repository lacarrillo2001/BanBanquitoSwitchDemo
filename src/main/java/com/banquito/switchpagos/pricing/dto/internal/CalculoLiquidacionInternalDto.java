package com.banquito.switchpagos.pricing.dto.internal;

import com.banquito.switchpagos.pricing.model.TarifaServicio;

import java.math.BigDecimal;

public record CalculoLiquidacionInternalDto(
        Integer transaccionesExitosas,
        Integer transaccionesFallidas,
        TarifaServicio tarifaServicio,
        BigDecimal tarifaUnitariaAplicada,
        BigDecimal ivaPorcentajeAplicado,
        BigDecimal subtotalComision,
        BigDecimal montoIva,
        BigDecimal totalDebitado
) {
}
