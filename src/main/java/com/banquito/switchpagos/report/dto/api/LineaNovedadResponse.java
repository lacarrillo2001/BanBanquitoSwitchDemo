package com.banquito.switchpagos.report.dto.api;

import java.math.BigDecimal;

public record LineaNovedadResponse(
        Integer secuencial,
        String estado,
        String codigoError,
        String mensajeError,
        BigDecimal monto,
        String cuentaDestino,
        String nombreBeneficiario
) {
}
