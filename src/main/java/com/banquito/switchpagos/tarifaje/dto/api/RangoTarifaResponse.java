package com.banquito.switchpagos.tarifaje.dto.api;

import java.math.BigDecimal;

public record RangoTarifaResponse(
        Integer rangoDesde,
        Integer rangoHasta,
        BigDecimal tarifaUnitaria
) {
}
