package com.banquito.switchpagos.integrationcore.dto.internal;

import java.math.BigDecimal;

public record ConsultaSaldoCoreResponse(
        Boolean exitoso,
        String codigo,
        String mensaje,
        BigDecimal saldoDisponible
) {
}
