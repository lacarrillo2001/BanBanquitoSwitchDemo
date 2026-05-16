package com.banquito.switchpagos.integrationcore.dto.internal;

import java.math.BigDecimal;
import java.util.UUID;

public record LiquidacionCoreRequest(
        UUID uuidGrupoOperacion,
        String cuentaMatriz,
        BigDecimal subtotalComision,
        BigDecimal montoIva,
        BigDecimal totalDebitado,
        Boolean permiteSobregiro,
        String codigoCuentaIngresos,
        String codigoCuentaIva,
        String referenciaExterna
) {
}
