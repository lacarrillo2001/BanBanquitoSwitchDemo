package com.banquito.switchpagos.processing.dto.internal;

import com.banquito.switchpagos.processing.enums.EstadoLineaPago;

import java.math.BigDecimal;

public record ResultadoLineaProcesamientoInternalDto(
        EstadoLineaPago estado,
        BigDecimal montoExitoso
) {
}
