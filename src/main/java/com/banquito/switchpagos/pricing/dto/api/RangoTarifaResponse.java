package com.banquito.switchpagos.pricing.dto.api;

import java.math.BigDecimal;

public record RangoTarifaResponse(
        Integer rangoDesde,
        Integer rangoHasta,
        BigDecimal tarifaUnitaria
) {
}
