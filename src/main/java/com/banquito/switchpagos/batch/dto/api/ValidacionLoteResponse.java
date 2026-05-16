package com.banquito.switchpagos.batch.dto.api;

import java.util.List;
import java.util.UUID;

public record ValidacionLoteResponse(
        UUID uuidLote,
        String estado,
        Boolean valido,
        TotalesValidacionResponse totales,
        List<ErrorGlobalResponse> errores
) {
}
