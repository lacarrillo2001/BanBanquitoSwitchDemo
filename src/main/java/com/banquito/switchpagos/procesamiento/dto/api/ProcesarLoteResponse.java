package com.banquito.switchpagos.procesamiento.dto.api;

import java.util.UUID;

public record ProcesarLoteResponse(
        UUID uuidLote,
        String estado,
        ResultadoProcesamientoResponse resultado,
        String siguienteAccion
) {
}
