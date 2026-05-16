package com.banquito.switchpagos.integrationcore.dto.internal;

import java.util.UUID;

public record LiquidacionCoreApiResponse(
        String estado,
        UUID uuidDebitoMatriz,
        UUID uuidCreditoIngresos,
        UUID uuidCreditoIva,
        UUID uuidGrupoOperacion
) {
}
