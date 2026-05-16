package com.banquito.switchpagos.integrationcore.dto.internal;

import java.math.BigDecimal;

public record SaldoCuentaCoreApiResponse(
        String numeroCuenta,
        String estado,
        BigDecimal saldoContable,
        BigDecimal saldoDisponible,
        Boolean permiteDebito
) {
}
