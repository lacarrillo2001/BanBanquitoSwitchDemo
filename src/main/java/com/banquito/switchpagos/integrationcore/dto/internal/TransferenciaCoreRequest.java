package com.banquito.switchpagos.integrationcore.dto.internal;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record TransferenciaCoreRequest(
        String cuentaOrigen,
        String cuentaDestino,
        String codigoSubtipo,
        BigDecimal monto,
        UUID uuidOperacion,
        UUID uuidGrupoOperacion,
        String referenciaExterna,
        String descripcion,
        String canalOrigen,
        LocalDate fechaNegocio,
        Integer usuarioCoreId,
        Integer credencialWebId
) {
}
