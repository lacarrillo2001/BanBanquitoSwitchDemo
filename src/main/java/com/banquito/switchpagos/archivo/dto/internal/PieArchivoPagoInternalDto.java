package com.banquito.switchpagos.archivo.dto.internal;

import java.math.BigDecimal;

public record PieArchivoPagoInternalDto(
        String hashPieControl,
        Integer totalRegistrosPie,
        BigDecimal montoTotalPie
) {
}
