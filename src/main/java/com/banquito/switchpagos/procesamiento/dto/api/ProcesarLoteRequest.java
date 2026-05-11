package com.banquito.switchpagos.procesamiento.dto.api;

public record ProcesarLoteRequest(
        String ejecutadoPor,
        String observacion
) {
}
