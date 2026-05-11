package com.banquito.switchpagos.archivo.dto.internal;

import java.math.BigDecimal;

public record DetalleArchivoPagoInternalDto(
        Integer secuencial,
        String identificacionBeneficiario,
        String nombreBeneficiario,
        String cuentaDestino,
        BigDecimal monto,
        String conceptoReferencia,
        String correoNotificacion
) {
}
