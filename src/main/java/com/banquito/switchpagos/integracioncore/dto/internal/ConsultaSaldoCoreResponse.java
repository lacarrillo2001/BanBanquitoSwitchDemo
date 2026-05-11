package com.banquito.switchpagos.integracioncore.dto.internal;

import java.math.BigDecimal;

public record ConsultaSaldoCoreResponse(
        Boolean exitoso,
        String codigo,
        String mensaje,
        BigDecimal saldoDisponible
) {
}
