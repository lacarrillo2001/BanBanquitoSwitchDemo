package com.banquito.switchpagos.pricing.dto.api;

import java.time.LocalDate;
import java.util.List;

public record TarifaServicioResponse(
        String tipoServicio,
        String moneda,
        LocalDate vigenteDesde,
        List<RangoTarifaResponse> rangos
) {
}
