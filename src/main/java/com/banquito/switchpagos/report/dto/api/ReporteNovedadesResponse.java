package com.banquito.switchpagos.report.dto.api;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public record ReporteNovedadesResponse(
        UUID uuidLote,
        String tipoReporte,
        String formato,
        OffsetDateTime fechaGeneracion,
        ResumenNovedadesResponse resumen,
        List<LineaNovedadResponse> lineas
) {
}
