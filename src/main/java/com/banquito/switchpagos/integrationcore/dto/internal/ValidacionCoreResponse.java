package com.banquito.switchpagos.integrationcore.dto.internal;

public record ValidacionCoreResponse(
        Boolean valida,
        String codigo,
        String mensaje
) {
}
