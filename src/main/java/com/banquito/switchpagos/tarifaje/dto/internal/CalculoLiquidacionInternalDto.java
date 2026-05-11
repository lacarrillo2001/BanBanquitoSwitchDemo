package com.banquito.switchpagos.tarifaje.dto.internal;

import com.banquito.switchpagos.tarifaje.model.TarifaServicio;

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
