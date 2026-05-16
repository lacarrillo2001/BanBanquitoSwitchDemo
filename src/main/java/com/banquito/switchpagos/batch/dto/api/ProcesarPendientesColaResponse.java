package com.banquito.switchpagos.batch.dto.api;

import java.util.List;

public record ProcesarPendientesColaResponse(
        Integer tomados,
        Integer completados,
        Integer fallidos,
        List<ResultadoProcesamientoColaResponse> resultados
) {
}
