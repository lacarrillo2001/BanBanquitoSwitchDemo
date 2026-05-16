package com.banquito.switchpagos.report.dto.api;

import java.time.OffsetDateTime;
import java.util.UUID;

public record ComprobanteLiquidacionResponse(
        UUID uuidLote,
        String tipoReporte,
        String formato,
        EmpresaComprobanteResponse empresa,
        ResumenPagosComprobanteResponse resumenPagos,
        LiquidacionComprobanteResponse liquidacionServicio,
        OffsetDateTime fechaGeneracion
) {
}
