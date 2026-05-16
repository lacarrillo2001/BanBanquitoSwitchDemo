package com.banquito.switchpagos.integrationcore.dto.internal;

import java.math.BigDecimal;
import java.util.UUID;

public record TransferenciaCoreResponse(
        String estado,
        UUID uuidDebitoCore,
        UUID uuidCreditoCore,
        UUID uuidGrupoOperacion,
        BigDecimal saldoDisponibleOrigen,
        String numeroComprobante
) {
}
