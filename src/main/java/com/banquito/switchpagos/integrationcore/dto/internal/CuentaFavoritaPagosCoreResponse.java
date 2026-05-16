package com.banquito.switchpagos.integrationcore.dto.internal;

import java.math.BigDecimal;

public record CuentaFavoritaPagosCoreResponse(
        String rucEmpresa,
        Boolean existe,
        String numeroCuenta,
        String estado,
        Boolean permiteDebito,
        BigDecimal saldoDisponible,
        Boolean esFavoritaPagos,
        Boolean valida,
        String codigo,
        String mensaje
) {
}
