package com.banquito.switchpagos.file.dto.internal;

import java.math.BigDecimal;

public record PieArchivoPagoInternalDto(
        String hashPieControl,
        Integer totalRegistrosPie,
        BigDecimal montoTotalPie
) {
}
