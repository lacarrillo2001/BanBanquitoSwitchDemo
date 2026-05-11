package com.banquito.switchpagos.procesamiento.dto.internal;

import com.banquito.switchpagos.procesamiento.enums.EstadoLineaPago;

import java.math.BigDecimal;

public record ResultadoLineaProcesamientoInternalDto(
        EstadoLineaPago estado,
        BigDecimal montoExitoso
) {
}
