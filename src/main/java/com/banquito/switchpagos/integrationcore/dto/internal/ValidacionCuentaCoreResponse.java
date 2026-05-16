package com.banquito.switchpagos.integrationcore.dto.internal;

public record ValidacionCuentaCoreResponse(
        Boolean valida,
        String codigo,
        String mensaje
) {
}
