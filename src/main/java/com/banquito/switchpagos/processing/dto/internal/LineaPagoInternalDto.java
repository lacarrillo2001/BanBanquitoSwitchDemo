package com.banquito.switchpagos.processing.dto.internal;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record LineaPagoInternalDto(
        Long idLinea,
        UUID uuidOperacionSwitch,
        Integer secuencial,
        String identificacionBeneficiario,
        String nombreBeneficiario,
        String cuentaDestino,
        BigDecimal monto,
        String conceptoReferencia,
        String correoNotificacion,
        String estado,
        String codigoError,
        String mensajeError,
        OffsetDateTime fechaValidacion
) {
}
