package com.banquito.switchpagos.integracioncore.dto.internal;

public record ValidacionCuentaCoreResponse(
        Boolean valida,
        String codigo,
        String mensaje
) {
}
