package com.banquito.switchpagos.integrationcore.dto.internal;

import java.math.BigDecimal;

public record ValidacionCuentaMatrizCoreApiResponse(
        String numeroCuenta,
        String rucEmpresa,
        Boolean existe,
        Boolean perteneceEmpresa,
        String estado,
        Boolean permiteDebito,
        BigDecimal saldoContable,
        BigDecimal saldoDisponible,
        Boolean permiteSobregiro,
        BigDecimal limiteSobregiro,
        Boolean valida,
        String codigo,
        String mensaje
) {
}
