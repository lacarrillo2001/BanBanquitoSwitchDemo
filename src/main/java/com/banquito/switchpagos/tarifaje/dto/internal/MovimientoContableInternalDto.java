package com.banquito.switchpagos.tarifaje.dto.internal;

import com.banquito.switchpagos.tarifaje.enums.ConceptoDetalleLiquidacion;

import java.math.BigDecimal;
import java.util.UUID;

public record MovimientoContableInternalDto(
        ConceptoDetalleLiquidacion concepto,
        BigDecimal monto,
        UUID uuidTransaccionCore,
        String cuentaOrigenCore,
        String cuentaDestinoCore,
        String estado
) {
}
