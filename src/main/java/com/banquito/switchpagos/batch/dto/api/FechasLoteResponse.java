package com.banquito.switchpagos.batch.dto.api;

import java.time.OffsetDateTime;

public record FechasLoteResponse(
        OffsetDateTime fechaRecepcion,
        OffsetDateTime fechaInicioValidacion,
        OffsetDateTime fechaFinValidacion,
        OffsetDateTime fechaInicioProceso,
        OffsetDateTime fechaFinProceso,
        OffsetDateTime fechaCierre
) {
}
