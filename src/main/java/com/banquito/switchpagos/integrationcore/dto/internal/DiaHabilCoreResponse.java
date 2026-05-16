package com.banquito.switchpagos.integrationcore.dto.internal;

import java.time.LocalDate;

public record DiaHabilCoreResponse(
        LocalDate fecha,
        Boolean esDiaHabil,
        Boolean esFinSemana,
        Boolean esFeriado,
        LocalDate siguienteDiaHabil,
        String codigo,
        String mensaje
) {
}
