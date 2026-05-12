package com.banquito.switchpagos.integrationcore.dto.internal;

import java.math.BigDecimal;
import java.util.UUID;

public record MovimientoCoreRequest(
        String cuentaOrigen,
        String cuentaDestino,
        BigDecimal monto,
        UUID uuidOperacionSwitch,
        UUID uuidGrupoCore,
        String concepto,
        Boolean permiteSobregiro
) {
}
