package com.banquito.switchpagos.batch.dto.api;

import java.util.UUID;

public record ResultadoProcesamientoColaResponse(
        Long idCola,
        UUID uuidLote,
        String estadoCola,
        String estadoLote,
        String codigo,
        String mensaje
) {
}
