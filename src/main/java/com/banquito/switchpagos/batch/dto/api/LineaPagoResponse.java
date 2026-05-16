package com.banquito.switchpagos.batch.dto.api;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record LineaPagoResponse(
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
