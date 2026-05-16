package com.banquito.switchpagos.batch.dto.api;

import java.util.UUID;

public record CargaLoteResponse(
        UUID uuidLote,
        String estado,
        String nombreArchivo,
        String hashArchivo,
        String mensaje,
        String siguienteAccion
) {
}
