package com.banquito.switchpagos.processing.dto.api;

public record ProcesarLoteRequest(
        String ejecutadoPor,
        String observacion
) {
}
