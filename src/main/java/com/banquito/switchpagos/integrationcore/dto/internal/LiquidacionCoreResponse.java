package com.banquito.switchpagos.integrationcore.dto.internal;

import java.util.UUID;

public record LiquidacionCoreResponse(
        Boolean exitoso,
        String codigo,
        String mensaje,
        UUID uuidDebitoMatriz,
        UUID uuidCreditoIngresos,
        UUID uuidCreditoIva,
        UUID uuidGrupoCore
) {
}
