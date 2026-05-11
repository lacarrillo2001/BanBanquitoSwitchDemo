package com.banquito.switchpagos.integracioncore.dto.internal;

import java.util.UUID;

public record MovimientoCoreResponse(
        Boolean exitoso,
        String codigo,
        String mensaje,
        UUID uuidTransaccionCore,
        UUID uuidGrupoCore
) {
}
