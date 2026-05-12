package com.banquito.switchpagos.batch.dto.api;

import java.util.UUID;

public record AnulacionLoteResponse(
        UUID uuidLote,
        String estado,
        String motivo
) {
}
