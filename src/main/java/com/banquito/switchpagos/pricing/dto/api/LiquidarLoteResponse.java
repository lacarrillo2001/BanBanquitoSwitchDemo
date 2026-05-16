package com.banquito.switchpagos.pricing.dto.api;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record LiquidarLoteResponse(
        UUID uuidLote,
        String estadoLiquidacion,
        Integer transaccionesExitosas,
        Integer transaccionesFallidas,
        BigDecimal tarifaUnitariaAplicada,
        BigDecimal ivaPorcentajeAplicado,
        BigDecimal subtotalComision,
        BigDecimal montoIva,
        BigDecimal totalDebitado,
        Boolean permiteSobregiro,
        List<MovimientoContableResponse> movimientosContables,
        String siguienteAccion
) {
}
