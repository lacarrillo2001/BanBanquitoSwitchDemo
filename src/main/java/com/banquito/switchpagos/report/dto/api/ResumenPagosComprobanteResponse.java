package com.banquito.switchpagos.report.dto.api;

import java.math.BigDecimal;

public record ResumenPagosComprobanteResponse(
        Integer transaccionesExitosas,
        Integer transaccionesRechazadas,
        BigDecimal montoTotalDispersado
) {
}
